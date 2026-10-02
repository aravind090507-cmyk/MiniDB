import java.util.*;

/**
 * Session — an active login session.
 * Expires after 30 minutes of inactivity.
 * canExecute() enforces role-based permissions.
 */
public class Session {
    // Session lasts 30 minutes
    private static final long TIMEOUT_MS = 30L * 60 * 1000;

    private String sessionId;
    private User user;
    private long loginTime;
    private long expiryTime;

    public Session(User user) {
        this.user = user;
        this.loginTime = System.currentTimeMillis();
        this.expiryTime = loginTime + TIMEOUT_MS;
        this.sessionId = "SES-" + loginTime;
    }

    // EXPIRY
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    // Reset timer on every activity
    public void refresh() {
        expiryTime = System.currentTimeMillis() + TIMEOUT_MS;
    }

    // PERMISSION CHECK
    public boolean canExecute(String cmdType) {
        if (isExpired()) return false;
        if (cmdType.equals("LOGOUT")) return true;
        switch (user.getRole().toUpperCase()) {
            case "ADMIN":
                return true; // full access
            case "MANAGER":
                // Can do everything EXCEPT create/drop tables
                return !cmdType.equals("CREATE") &&
                       !cmdType.equals("DROP") &&
                       !cmdType.equals("CREATE_INDEX") &&
                       !cmdType.equals("CREATE_USER") &&
                       !cmdType.equals("CHANGE_ROLE") &&
                       !cmdType.equals("LIST_USERS");
            case "STAFF":
                // Can only read, insert, and use transactions
                return cmdType.equals("SELECT") ||
                       cmdType.equals("INSERT") ||
                       cmdType.equals("SHOW_TABLES") ||
                       cmdType.equals("SEARCH") ||
                       cmdType.equals("BEGIN_TXN") ||
                       cmdType.equals("COMMIT_TXN") ||
                       cmdType.equals("ROLLBACK_TXN") ||
                       cmdType.equals("TXN_STMT") ||
                       cmdType.equals("LIST_TXN");
            case "VIEWER":
                // Read-only
                return cmdType.equals("SELECT") ||
                       cmdType.equals("SHOW_TABLES") ||
                       cmdType.equals("SEARCH");
            default:
                return false;
        }
    }

    // GETTERS
    public String getSessionId() { return sessionId; }
    public User getUser() { return user; }
    public String getUsername() { return user.getUsername(); }
    public String getRole() { return user.getRole(); }

    public long minutesRemaining() {
        long ms = expiryTime - System.currentTimeMillis();
        return Math.max(0, ms / 60000);
    }
}
