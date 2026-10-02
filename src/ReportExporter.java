import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ReportExporter — writes report output to disk.
 * Supports plain text (.txt) and CSV (.csv) formats.
 */
public class ReportExporter {
    private String exportFolder;

    public ReportExporter(String folder) {
        this.exportFolder = folder;
        new File(folder).mkdirs();
    }

    // ■■ EXPORT TO TEXT FILE ■■■■■■■■■■■■■■■■■■■■■■■■
    public String exportText(String reportContent, String reportType) {
        String timestamp = new SimpleDateFormat(
            "yyyyMMdd_HHmmss").format(new Date());
        String filename = exportFolder +
            reportType + "_" + timestamp + ".txt";

        try (PrintWriter pw = new PrintWriter(
             new FileWriter(filename))) {
            pw.println("Generated: " + new Date());
            pw.println("Report : " + reportType);
            pw.println();
            pw.print(reportContent);
        } catch (IOException e) {
            return "Export error: " + e.getMessage();
        }
        return "Report saved: " + filename;
    }

    // ■■ EXPORT TABLE TO CSV ■■■■■■■■■■■■■■■■■■■■■■■■
    // Exports a full table as a downloadable CSV.
    public String exportTableCSV(Table table) {
        String timestamp = new SimpleDateFormat(
            "yyyyMMdd_HHmmss").format(new Date());
        String filename = exportFolder +
            table.getName() + "_export_" +
            timestamp + ".csv";

        try (BufferedWriter bw = new BufferedWriter(
             new FileWriter(filename))) {
            // Header
            bw.write(String.join(",",
                table.getSchema()));
            bw.newLine();
            // Rows
            for (Row row : table.getRows()) {
                List<String> vals = new ArrayList<>();
                for (String col : table.getSchema())
                    vals.add(row.getValue(col));
                bw.write(String.join(",", vals));
                bw.newLine();
            }
        } catch (IOException e) {
            return "Export error: " + e.getMessage();
        }
        return "CSV exported: " + filename;
    }

    // ■■ LIST ALL EXPORTED REPORTS ■■■■■■■■■■■■■■■■■■
    public List<String> listExports() {
        List<String> names = new ArrayList<>();
        File folder = new File(exportFolder);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files)
                names.add(f.getName());
            Collections.sort(names,
                Collections.reverseOrder());
        }
        return names;
    }
}
