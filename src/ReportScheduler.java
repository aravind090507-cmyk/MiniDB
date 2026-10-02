import java.util.*;
import java.text.SimpleDateFormat;

/**
 * ReportScheduler — automatically generates reports
 * on a daily schedule using Java Timer.
 * Configured to run at midnight every day.
 */
public class ReportScheduler {
    private Timer timer;
    private ReportEngine engine;
    private ReportExporter exporter;

    public ReportScheduler(ReportEngine engine,
                           ReportExporter exporter) {
        this.engine = engine;
        this.exporter = exporter;
        this.timer = new Timer("ReportScheduler", true);
    }

    // ■■ START DAILY SCHEDULE ■■■■■■■■■■■■■■■■■■■■■■
    // Generates 3 reports automatically every day.
    public void startDailySchedule() {
        // Calculate time until midnight (23:59:00 as per spec/screenshot)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);

        long delay = cal.getTimeInMillis() -
            System.currentTimeMillis();
        long period = 24L * 60 * 60 * 1000; // 24 hours

        if (delay <= 0) {
            delay += period;
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runDailyReports();
            }
        }, delay, period);

        System.out.println(
            "Daily report schedule started.");
    }

    // ■■ RUN ALL DAILY REPORTS ■■■■■■■■■■■■■■■■■■■■■
    private void runDailyReports() {
        String today = new SimpleDateFormat("yyyy-MM-dd")
            .format(new Date());
        System.out.println("Running daily reports: " + today);

        // 1. Daily sales report
        String sales = engine.generate("DAILY_SALES", today);
        exporter.exportText(sales, "daily_sales");

        // 2. Low stock alert
        String stock = engine.generate("LOW_STOCK", "10");
        exporter.exportText(stock, "low_stock_alert");

        // 3. Revenue summary
        String revenue = engine.generate("REVENUE", null);
        exporter.exportText(revenue, "revenue_summary");

        System.out.println("Daily reports saved to /reports/");
    }

    // ■■ RUN REPORT RIGHT NOW ■■■■■■■■■■■■■■■■■■■■■■
    public String runNow(String reportType, String param) {
        String content = engine.generate(reportType, param);
        return exporter.exportText(content, reportType);
    }

    public void stop() {
        timer.cancel();
        System.out.println("Report scheduler stopped.");
    }
}
