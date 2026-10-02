import java.util.*;

/**
 * Transaction — one unit of work.
 * Holds all SQL statements queued before COMMIT.
 * Status: ACTIVE -> COMMITTED or ROLLED_BACK.
 */
public class Transaction {
    public enum Status {
        ACTIVE, COMMITTED, ROLLED_BACK
    }

    private String id;
    private List<String> operations; // forward ops
    private List<String> rollbackOps; // reverse ops
    private Status status;
    private long startTime;

    public Transaction(String id) {
        this.id = id;
        this.operations = new ArrayList<>();
        this.rollbackOps = new ArrayList<>();
        this.status = Status.ACTIVE;
        this.startTime = System.currentTimeMillis();
    }

    // Add a SQL statement to execute on COMMIT
    public void addOp(String sql) {
        operations.add(sql);
    }

    // Add a reverse statement for ROLLBACK
    // e.g. INSERT -> rollback is DELETE
    public void addRollbackOp(String sql) {
        rollbackOps.add(0, sql); // LIFO order
    }

    public void commit() {
        status = Status.COMMITTED;
    }

    public void rollback() {
        status = Status.ROLLED_BACK;
    }

    public boolean isActive() { return status == Status.ACTIVE; }
    public String getId() { return id; }
    public Status getStatus() { return status; }
    public List<String> getOps() { return operations; }
    public List<String> getRollbackOps() { return rollbackOps; }

    public long elapsedMs() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public String toString() {
        return id + " [" + status + "] " + operations.size() + " op(s)";
    }
}
