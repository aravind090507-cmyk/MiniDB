import java.util.*;

/**
 * TransactionManager — orchestrates the full lifecycle.
 * BEGIN -> creates Transaction, writes to WAL.
 * ADD -> queues SQL in memory, locks table, logs PENDING.
 * COMMIT -> executes all ops via DBEngine, saves to disk.
 * ROLLBACK -> discards all ops, releases locks.
 * RECOVER -> on startup rolls back any incomplete txns.
 */
public class TransactionManager {
    private HashMap<String, Transaction> active;
    private TransactionLog log;
    private LockManager locks;
    private DBEngine engine;
    private int counter = 1;

    public TransactionManager(DBEngine engine, String logPath) {
        this.engine = engine;
        this.active = new HashMap<>();
        this.log = new TransactionLog(logPath);
        this.locks = new LockManager();
        recover();
    }

    // BEGIN
    public String begin() {
        String id = "TXN-" + counter++;
        Transaction txn = new Transaction(id);
        active.put(id, txn);
        log.write(id, "BEGIN", "BEGIN");
        return "Transaction started: " + id;
    }

    // ADD STATEMENT
    // Queues a SQL statement into the transaction.
    // Does NOT execute it yet.
    public String add(String txnId, String sql) {
        Transaction txn = active.get(txnId);
        if (txn == null)
            return "Unknown transaction: " + txnId;
        if (!txn.isActive())
            return "Transaction not active: " + txnId;

        // Lock the target table
        String tbl = extractTable(sql);
        if (!locks.lock(tbl, txnId))
            return "Cannot lock table: " + tbl + ". Held by: " + locks.getHolder(tbl);

        txn.addOp(sql);
        log.write(txnId, sql, "PENDING");
        return "Queued [" + txnId + "]: " + sql;
    }

    // COMMIT
    // Executes all queued ops via DBEngine.
    // Writes to disk only after ALL ops succeed.
    public String commit(String txnId) {
        Transaction txn = active.get(txnId);
        if (txn == null)
            return "Unknown transaction: " + txnId;
        if (!txn.isActive())
            return "Transaction not active.";

        List<String> ops = txn.getOps();
        List<String> done = new ArrayList<>();
        try {
            for (String op : ops) {
                String result = engine.execute(op);
                // Check for failure
                if (result.toLowerCase().contains("error") ||
                    result.toLowerCase().contains("not found")) {
                    // Auto-rollback on failure
                    log.write(txnId, "Error on: " + op, "ROLLBACK");
                    txn.rollback();
                    locks.releaseAll(txnId);
                    active.remove(txnId);
                    return "Transaction ROLLED BACK (error): " + result;
                }
                done.add(op);
            }
            // All succeeded
            txn.commit();
            log.write(txnId, "ALL " + ops.size() + " ops", "COMMIT");
            locks.releaseAll(txnId);
            active.remove(txnId);
            return "Transaction COMMITTED: " + txnId + " (" + ops.size() + " operations)";
        } catch (Exception e) {
            txn.rollback();
            locks.releaseAll(txnId);
            active.remove(txnId);
            return "Transaction ROLLED BACK (exception): " + e.getMessage();
        }
    }

    // ROLLBACK
    // Discards all queued operations, releases locks.
    public String rollback(String txnId) {
        Transaction txn = active.get(txnId);
        if (txn == null)
            return "Unknown transaction: " + txnId;
        txn.rollback();
        log.write(txnId, "ALL", "ROLLBACK");
        locks.releaseAll(txnId);
        active.remove(txnId);
        return "Transaction ROLLED BACK: " + txnId;
    }

    // CRASH RECOVERY
    // Called on startup. Finds any incomplete txns.
    private void recover() {
        List<String> incomplete = log.findIncomplete();
        if (incomplete.isEmpty()) return;
        System.out.println("RECOVERY: Found " + incomplete.size() + " incomplete transaction(s):");
        for (String id : incomplete) {
            System.out.println("  Auto-rolled back: " + id);
        }
    }

    // STATUS
    public int activeCount() { return active.size(); }

    public void printActive() {
        if (active.isEmpty()) {
            System.out.println("No active transactions.");
            return;
        }
        active.values().forEach(t -> System.out.println(" " + t));
    }

    // EXTRACT TABLE NAME FROM SQL
    private String extractTable(String sql) {
        String up = sql.toUpperCase().trim();
        String[] kw = {"FROM ", "INTO ", "UPDATE ", "TABLE "};
        for (String k : kw) {
            int i = up.indexOf(k);
            if (i > -1) {
                String rest = sql.substring(i + k.length()).trim();
                return rest.split("[\\s(]")[0].trim();
            }
        }
        return "unknown";
    }
}
