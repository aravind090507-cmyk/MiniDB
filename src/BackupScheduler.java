import java.util.*;

/**
 * BackupScheduler — runs automatic backups every 24 hours.
 * Also prunes old backups keeping only the last 7.
 */
public class BackupScheduler {
    private Timer timer;
    private BackupManager manager;
    private int keepBackups;

    public BackupScheduler(BackupManager manager,
                           int keepBackups) {
        this.manager = manager;
        this.keepBackups = keepBackups;
        this.timer =
            new Timer("BackupScheduler", true);
    }

    // ■■ START AUTO BACKUP ■■■■■■■■■■■■■■■■■■■■■■■■■
    // Backs up immediately, then every 24 hours.
    public void start() {
        long period = 24L * 60 * 60 * 1000; // 24 hours
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String result = manager.createBackup();
                System.out.println(
                    "[AutoBackup] " + result);
                // Prune old backups
                String prune =
                    manager.pruneOldBackups(keepBackups);
                System.out.println(
                    "[AutoBackup] " + prune);
            }
        }, 0, period);

        System.out.println("Auto backup scheduled every 24h.");
        System.out.println("Keeping last " + keepBackups + " backups.");
    }

    // ■■ CHANGE INTERVAL ■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // Restart scheduler with a new interval in hours.
    public void restart(int intervalHours) {
        timer.cancel();
        timer = new Timer("BackupScheduler", true);
        long period = (long) intervalHours * 60 * 60 * 1000;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(
                    "[AutoBackup] " +
                        manager.createBackup());
                System.out.println(
                    "[AutoBackup] " +
                        manager.pruneOldBackups(keepBackups));
            }
        }, 0, period);

        System.out.println("Backup interval set to " +
            intervalHours + "h.");
    }

    public void stop() {
        timer.cancel();
        System.out.println("Auto backup stopped.");
    }
}
