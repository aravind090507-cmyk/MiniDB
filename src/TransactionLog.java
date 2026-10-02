import java.io.*;
import java.util.*;

/**
 * TransactionLog — Write-Ahead Log (WAL).
 * Every operation is written to the log BEFORE execution.
 * On crash recovery, incomplete transactions are detected
 * and automatically rolled back.
 */
public class TransactionLog {
    private String logPath;

    public TransactionLog(String logPath) {
        this.logPath = logPath;
    }

    // Write log entry: status: BEGIN | PENDING | COMMIT | ROLLBACK
    public void write(String txnId, String operation, String status) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logPath, true))) {
            pw.printf("[%s] %s | %-10s | %s%n",
                new java.util.Date(), txnId, status, operation);
        } catch (IOException e) {
            System.err.println("WAL write error: " + e.getMessage());
        }
    }

    // Returns list of TXN-IDs that have no COMMIT entry.
    public List<String> findIncomplete() {
        Set<String> begun = new LinkedHashSet<>();
        Set<String> committed = new HashSet<>();
        File f = new File(logPath);
        if (!f.exists()) return new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("| BEGIN")) {
                    String id = extractId(line);
                    if (!id.isEmpty()) begun.add(id);
                } else if (line.contains("| COMMIT")) {
                    committed.add(extractId(line));
                } else if (line.contains("| ROLLBACK")) {
                    committed.add(extractId(line));
                }
            }
        } catch (IOException e) {
            System.err.println("WAL read error: " + e.getMessage());
        }
        begun.removeAll(committed);
        return new ArrayList<>(begun);
    }

    // Extract TXN-NNN from a log line
    private String extractId(String line) {
        int start = line.indexOf("TXN-");
        if (start < 0) return "";
        int end = line.indexOf(" ", start);
        return (end < 0) ? line.substring(start) : line.substring(start, end);
    }

    // Archive log after successful COMMIT (keeps log small)
    public void archiveLog() {
        File f = new File(logPath);
        if (f.exists()) {
            File archive = new File(logPath + ".bak");
            f.renameTo(archive);
        }
    }

    public void clearLog() {
        new File(logPath).delete();
    }
}
