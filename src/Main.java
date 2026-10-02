import java.util.Scanner;

/**
 * Main.java — updated CLI that requires login.
 * Tracks sessionId after LOGIN.
 * Passes sessionId to every engine.execute() call.
 */
public class Main {
    public static void main(String[] args) {
        DBEngine engine = new DBEngine("data/");
        Scanner sc = new Scanner(System.in);
        String sid = null; // current session ID

        System.out.println("■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");
        System.out.println("■ Mini Java DB v1.0 ■");
        System.out.println("■ Type: LOGIN <user> <pass> ■");
        System.out.println("■ Type: EXIT to quit ■");
        System.out.println("■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");

        while (true) {
            // Show different prompt based on login state
            if (sid == null)
                System.out.print("\n[not logged in]> ");
            else
                System.out.print("\nminidb> ");

            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;

            // EXIT
            if (input.equalsIgnoreCase("EXIT")) {
                System.out.println("Goodbye!");
                break;
            }

            // SHOW TABLES (shortcut)
            if (input.equalsIgnoreCase("SHOW TABLES")) {
                String r = engine.execute("SHOW TABLES", sid);
                if (!r.isEmpty()) {
                    System.out.println(r);
                }
                continue;
            }

            // Pass all commands to engine
            String result = engine.execute(input, sid);

            // If LOGIN succeeded, capture session ID
            if (input.toUpperCase().startsWith("LOGIN") && result.contains("Session: ")) {
                int start = result.indexOf("Session: ") + 9;
                int end = result.indexOf(" ", start);
                sid = (end > start) ? result.substring(start, end) : result.substring(start);
            }

            // If LOGOUT, clear session
            if (input.equalsIgnoreCase("LOGOUT")) {
                sid = null;
            }

            if (!result.isEmpty())
                System.out.println(result);
        }
        sc.close();
    }
}
