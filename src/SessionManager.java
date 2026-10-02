import java.util.*;

/**
 * SessionManager — tracks all active login sessions.
 * Sessions are stored in memory.
 * Expired sessions are cleaned up automatically.
 */
public class SessionManager {
    // sessionId -> Session
    private HashMap<String, Session> sessions;

    public SessionManager() {
        sessions = new HashMap<>();
    }

    // Called right after successful login.
    // Removes any existing session for the same user.
    public void add(Session session) {
        // Remove old session if user re-logs in
        sessions.values().removeIf(s -> s.getUsername().equals(session.getUsername()));
        sessions.put(session.getSessionId(), session);
        System.out.println("Session registered: " + session.getSessionId() + " for " + session.getUsername());
    }

    // Returns null if session missing or expired.
    public Session get(String sessionId) {
        Session s = sessions.get(sessionId);
        if (s == null) return null;
        if (s.isExpired()) {
            sessions.remove(sessionId);
            System.out.println("Session expired: " + sessionId);
            return null;
        }
        s.refresh(); // reset expiry on use
        return s;
    }

    public String logout(String sessionId) {
        Session s = sessions.remove(sessionId);
        if (s == null) return "Session not found.";
        return "Logged out: " + s.getUsername();
    }

    public int cleanExpired() {
        int before = sessions.size();
        sessions.entrySet().removeIf(e -> e.getValue().isExpired());
        return before - sessions.size();
    }

    public int activeCount() { return sessions.size(); }

    public void printActive() {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions.");
            return;
        }
        sessions.values().forEach(s ->
            System.out.println(" " + s.getSessionId() +
                " | " + s.getUsername() +
                " [" + s.getRole() + "]" +
                " | " + s.minutesRemaining() + " min left")
        );
    }
}
