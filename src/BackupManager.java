import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * BackupManager — creates, lists, and restores backups.
 * Each backup is a timestamped folder containing copies
 * of all CSV table files.
 */
public class BackupManager {
    private String dataFolder;
    private String backupRoot;

    public BackupManager(String dataFolder,
                         String backupRoot) {
        this.dataFolder = dataFolder;
        this.backupRoot = backupRoot;
        new File(backupRoot).mkdirs();
    }

    // ■■ CREATE BACKUP ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // Copies all .csv files to a new timestamped folder.
    public String createBackup() {
        String timestamp = new SimpleDateFormat(
            "yyyyMMdd_HHmmss").format(new Date());
        String backupPath = backupRoot +
            "backup_" + timestamp + "/";
        File backupDir = new File(backupPath);
        backupDir.mkdirs();

        File dataDir = new File(dataFolder);
        File[] csvFiles = dataDir.listFiles(
            (d, n) -> n.endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0)
            return "No data files found to backup.";

        int count = 0;
        for (File csv : csvFiles) {
            try {
                copyFile(csv,
                    new File(backupPath + csv.getName()));
                count++;
            } catch (IOException e) {
                return "Backup error copying " +
                    csv.getName() + ": " + e.getMessage();
            }
        }

        // Write backup metadata file
        writeBackupInfo(backupPath, timestamp, count);
        return "Backup created: backup_" + timestamp +
            " (" + count + " table files)";
    }

    // ■■ RESTORE FROM BACKUP ■■■■■■■■■■■■■■■■■■■■■■■■
    // Copies CSV files from backup folder back to data folder.
    public String restore(String backupId) {
        String backupPath = backupRoot + backupId + "/";
        File backupDir = new File(backupPath);
        if (!backupDir.exists())
            return "Backup not found: " + backupId;

        File[] csvFiles = backupDir.listFiles(
            (d, n) -> n.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0)
            return "No CSV files found in backup: " + backupId;

        // Integrity check before restore
        IntegrityChecker checker = new IntegrityChecker();
        for (File csv : csvFiles) {
            String result = checker.checkFile(csv);
            if (!result.equals("OK"))
                return "Backup file corrupted: " +
                    csv.getName() + " — " + result;
        }

        // All files OK — proceed with restore
        int count = 0;
        for (File csv : csvFiles) {
            try {
                copyFile(csv,
                    new File(dataFolder + csv.getName()));
                count++;
            } catch (IOException e) {
                return "Restore error: " + e.getMessage();
            }
        }
        return "Restore complete from " + backupId +
            " (" + count + " tables restored). " +
            "RESTART the application to reload data.";
    }

    // ■■ LIST ALL BACKUPS ■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public List<String> listBackups() {
        List<String> backups = new ArrayList<>();
        File root = new File(backupRoot);
        File[] dirs = root.listFiles(File::isDirectory);
        if (dirs != null) {
            Arrays.sort(dirs, Comparator.reverseOrder());
            for (File d : dirs)
                backups.add(d.getName());
        }
        return backups;
    }

    // ■■ DELETE OLD BACKUPS ■■■■■■■■■■■■■■■■■■■■■■■■■
    // Keeps only the most recent N backups.
    public String pruneOldBackups(int keepCount) {
        List<String> all = listBackups();
        if (all.size() <= keepCount)
            return "No old backups to remove.";

        int deleted = 0;
        for (int i = keepCount; i < all.size(); i++) {
            String path = backupRoot + all.get(i) + "/";
            if (deleteFolder(new File(path)))
                deleted++;
        }
        return "Removed " + deleted + " old backup(s).";
    }

    // ■■ BACKUP INFO FILE ■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void writeBackupInfo(String path,
                                 String ts, int count) {
        try (PrintWriter pw = new PrintWriter(
             new FileWriter(path + "backup_info.txt"))) {
            pw.println("Backup ID : backup_" + ts);
            pw.println("Created : " + new Date());
            pw.println("Files : " + count);
            pw.println("Data Folder: " + dataFolder);
        } catch (IOException e) {
            System.err.println("Could not write backup info.");
        }
    }

    // ■■ FILE COPY UTILITY ■■■■■■■■■■■■■■■■■■■■■■■■■■
    private void copyFile(File src, File dst)
        throws IOException {
        try (InputStream in =
                 new FileInputStream(src);
             OutputStream out =
                 new FileOutputStream(dst)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
        }
    }

    // ■■ FOLDER DELETE UTILITY ■■■■■■■■■■■■■■■■■■■■■■
    private boolean deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null)
                for (File f : files)
                    deleteFolder(f);
        }
        return folder.delete();
    }
}
