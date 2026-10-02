import java.io.*;
import java.util.*;

/**
 * IntegrityChecker — validates CSV files before restoring.
 * Checks: file exists, not empty, columns consistent.
 * Returns "OK" if valid, or an error message.
 */
public class IntegrityChecker {
    // ■■ CHECK A SINGLE FILE ■■■■■■■■■■■■■■■■■■■■■■■■
    public String checkFile(File csvFile) {
        if (!csvFile.exists())
            return "File does not exist.";
        if (csvFile.length() == 0)
            return "File is empty.";

        try (BufferedReader br = new BufferedReader(
             new FileReader(csvFile))) {
            String header = br.readLine();
            if (header == null || header.trim().isEmpty())
                return "Missing header row.";

            int colCount = header.split(",").length;
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;
                int cols = line.split(",", -1).length;
                if (cols != colCount)
                    return "Row " + lineNum +
                        " has " + cols + " columns," +
                        " expected " + colCount + ".";
            }
        } catch (IOException e) {
            return "IO error: " + e.getMessage();
        }
        return "OK";
    }

    // ■■ CHECK ALL CSV FILES IN FOLDER ■■■■■■■■■■■■■■
    public Map<String, String> checkFolder(String folder) {
        Map<String, String> results = new LinkedHashMap<>();
        File dir = new File(folder);
        File[] csvFiles = dir.listFiles(
            (d, n) -> n.endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0) {
            results.put("_status", "No CSV files found.");
            return results;
        }

        for (File f : csvFiles)
            results.put(f.getName(), checkFile(f));

        return results;
    }

    // ■■ CHECK LIVE DATABASE ■■■■■■■■■■■■■■■■■■■■■■■■
    public String checkDatabase(
        HashMap<String, Table> database) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DATABASE INTEGRITY CHECK ===\n");
        for (Map.Entry<String, Table> entry :
             database.entrySet()) {
            Table t = entry.getValue();
            List<String> schema = t.getSchema();
            int errors = 0;

            for (Row row : t.getRows()) {
                for (String col : schema) {
                    if (row.getValue(col) == null) {
                        errors++;
                        break;
                    }
                }
            }

            String status = (errors == 0) ? "OK" :
                errors + " row(s) with missing values";
            sb.append(String.format(
                " %-20s : %s\n",
                entry.getKey(), status));
        }
        return sb.toString();
    }
}
