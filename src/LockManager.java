import java.util.*;

/**
 * LockManager — table-level locking.
 * Ensures only ONE transaction can write to a table
 * at a time. Prevents dirty writes in concurrent use.
 */
public class LockManager {
    // tableName -> txnId that holds the lock
    private final HashMap<String, String> locks;

    public LockManager() {
        locks = new HashMap<>();
    }

    // Returns true if lock acquired, false if blocked.
    public synchronized boolean lock(String tableName, String txnId) {
        String holder = locks.get(tableName);
        if (holder == null) {
            locks.put(tableName, txnId);
            System.out.println("LOCK acquired: " + tableName + " by " + txnId);
            return true;
        }
        if (holder.equals(txnId)) {
            return true; // already own this lock
        }
        System.out.println("LOCK blocked: " + tableName + " held by " + holder);
        return false;
    }

    public synchronized void releaseAll(String txnId) {
        locks.entrySet().removeIf(e -> e.getValue().equals(txnId));
        System.out.println("LOCKS released: " + txnId);
    }

    public boolean isLocked(String tableName) {
        return locks.containsKey(tableName);
    }

    public String getHolder(String tableName) {
        return locks.getOrDefault(tableName, "none");
    }

    public int lockCount() {
        return locks.size();
    }

    public void printLocks() {
        System.out.println("Active locks: " + locks);
    }
}
