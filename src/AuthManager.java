import java.security.MessageDigest;
import java.io.*;
import java.util.*;

/**
 * AuthManager — user management and authentication.
 * Hashes passwords with SHA-256 (java.security, no libs).
 * Persists all users to users.csv.
 */
public class AuthManager {
    private HashMap<String, User> users; // username -> User
    private String usersFile;

    public AuthManager(String usersFile) {
        this.usersFile = usersFile;
        this.users = new HashMap<>();
        loadUsers();
        ensureDefaultAdmin();
    }

    // SHA-256 HASH
    // Uses pure Java standard library — no external deps.
    public String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error: " + e.getMessage());
        }
    }

    // DEFAULT ADMIN
    private void ensureDefaultAdmin() {
        if (!users.isEmpty()) return;
        createUser("admin", "admin123", "ADMIN");
        System.out.println("Default admin created.");
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
        System.out.println("PLEASE CHANGE PASSWORD IMMEDIATELY.");
    }

    // CREATE USER
    public String createUser(String username, String password, String role) {
        if (username == null || username.isEmpty())
            return "Username cannot be empty.";
        if (users.containsKey(username))
            return "User already exists: " + username;

        String[] validRoles = {"ADMIN", "MANAGER", "STAFF", "VIEWER"};
        boolean roleOk = false;
        for (String r : validRoles)
            if (r.equalsIgnoreCase(role)) roleOk = true;
        if (!roleOk)
            return "Invalid role: " + role + ". Use ADMIN/MANAGER/STAFF/VIEWER";

        String id = "USR-" + (users.size() + 1);
        String hash = hash(password);
        User user = new User(id, username, hash, role.toUpperCase());
        users.put(username, user);
        saveUsers();
        return "User created: " + username + " [" + role.toUpperCase() + "]";
    }

    // LOGIN
    // Returns a Session on success, null on failure.
    public Session login(String username, String password) {
        User user = users.get(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return null;
        }
        if (!user.isActive()) {
            System.out.println("Account disabled: " + username);
            return null;
        }
        if (!hash(password).equals(user.getPasswordHash())) {
            System.out.println("Wrong password.");
            return null;
        }
        System.out.println("Login OK: " + username + " [" + user.getRole() + "]");
        return new Session(user);
    }

    // CHANGE PASSWORD
    public String changePassword(String username, String oldPass, String newPass) {
        User user = users.get(username);
        if (user == null) return "User not found.";
        if (!hash(oldPass).equals(user.getPasswordHash()))
            return "Wrong current password.";
        user.setPasswordHash(hash(newPass));
        saveUsers();
        return "Password changed for: " + username;
    }

    // CHANGE ROLE
    public String changeRole(String username, String newRole) {
        User user = users.get(username);
        if (user == null) return "User not found.";
        String old = user.getRole();
        user.setRole(newRole.toUpperCase());
        saveUsers();
        return "Role changed: " + username + " " + old + " -> " + newRole.toUpperCase();
    }

    // DISABLE USER
    public String disableUser(String username) {
        User user = users.get(username);
        if (user == null) return "User not found.";
        if (user.getRole().equals("ADMIN"))
            return "Cannot disable an ADMIN account.";
        user.setActive(false);
        saveUsers();
        return "Account disabled: " + username;
    }

    // ENABLE USER
    public String enableUser(String username) {
        User user = users.get(username);
        if (user == null) return "User not found.";
        user.setActive(true);
        saveUsers();
        return "Account enabled: " + username;
    }

    // LIST USERS
    public List<User> listUsers() {
        return new ArrayList<>(users.values());
    }

    // SAVE TO CSV
    private void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersFile))) {
            for (User u : users.values()) {
                bw.write(u.toCSV());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Save users error: " + e.getMessage());
        }
    }

    // LOAD FROM CSV
    private void loadUsers() {
        File f = new File(usersFile);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    User u = User.fromCSV(line);
                    users.put(u.getUsername(), u);
                }
            }
            System.out.println("Loaded " + users.size() + " user(s) from " + usersFile);
        } catch (IOException e) {
            System.err.println("Load users error: " + e.getMessage());
        }
    }
}
