import java.util.*;

/**
 * User — a database user account.
 * Passwords stored as SHA-256 hash ONLY, never plain text.
 */
public class User {
    private String userId;
    private String username;
    private String passwordHash; // SHA-256 hex
    private String role; // ADMIN/MANAGER/STAFF/VIEWER
    private String createdDate;
    private boolean active;

    public User(String userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdDate = new java.util.Date().toString();
        this.active = true;
    }

    // GETTERS
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash(){ return passwordHash; }
    public String getRole() { return role; }
    public String getCreatedDate() { return createdDate; }
    public boolean isActive() { return active; }

    // SETTERS
    public void setRole(String r) { role = r; }
    public void setActive(boolean a) { active = a; }
    public void setPasswordHash(String h) { passwordHash = h; }

    // SERIALIZATION
    // Convert to CSV line for file storage.
    public String toCSV() {
        return String.join(",",
            userId, username, passwordHash,
            role, createdDate,
            String.valueOf(active));
    }

    // Create User from a CSV line.
    public static User fromCSV(String line) {
        String[] p = line.split(",", 6);
        User u = new User(p[0], p[1], p[2], p[3]);
        // p[4] = createdDate (already set in constructor)
        u.createdDate = p[4];
        u.active = Boolean.parseBoolean(p[5]);
        return u;
    }

    @Override
    public String toString() {
        return userId + " | " + username + " | " + role + " | active=" + active;
    }
}
