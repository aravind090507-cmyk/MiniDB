import java.util.*;

/**
 * ReportEngine — generates business reports by querying
 * the in-memory tables through DBEngine.
 * All reports return a formatted String.
 */
public class ReportEngine {
    private HashMap<String, Table> database;

    public ReportEngine(HashMap<String, Table> database) {
        this.database = database;
    }

    // ■■ MAIN DISPATCHER ■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public String generate(String reportType, String param) {
        switch (reportType.toUpperCase()) {
            case "DAILY_SALES":
                return dailySalesReport(param);
            case "MONTHLY":
                return monthlyReport(param);
            case "LOW_STOCK":
                int threshold = parseIntSafe(param, 10);
                return lowStockReport(threshold);
            case "STOCK_VALUE":
                return stockValueReport();
            case "TOP_PRODUCTS":
                int topN = parseIntSafe(param, 5);
                return topProductsReport(topN);
            case "CUSTOMER_HISTORY":
                return customerHistoryReport(param);
            case "TOP_CUSTOMERS":
                int topC = parseIntSafe(param, 5);
                return topCustomersReport(topC);
            case "REVENUE":
                return revenueReport();
            case "INVENTORY":
                return inventoryReport();
            default:
                return "Unknown report type: " + reportType;
        }
    }

    // ■■ DAILY SALES REPORT ■■■■■■■■■■■■■■■■■■■■■■■■■
    // Shows all sales on a specific date.
    // Param: date string e.g. "2024-01-15"
    private String dailySalesReport(String date) {
        Table sales = database.get("sales");
        if (sales == null)
            return "Sales table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" DAILY SALES REPORT — " + date + "\n");
        sb.append(divider());

        double totalRevenue = 0;
        int saleCount = 0;

        for (Row row : sales.getRows()) {
            if (row.getValue("date").equals(date)) {
                sb.append(String.format(
                    " Sale#%-5s Product:%-5s Customer:%-5s Qty:%-4s Total:%-8s\n",
                    row.getValue("id"),
                    row.getValue("product_id"),
                    row.getValue("customer_id"),
                    row.getValue("qty"),
                    row.getValue("total")));
                totalRevenue += toDouble(row.getValue("total"));
                saleCount++;
            }
        }

        if (saleCount == 0)
            sb.append(" No sales found for: " + date + "\n");

        sb.append(divider());
        sb.append(String.format(
            " Total Transactions : %d\n", saleCount));
        sb.append(String.format(
            " Total Revenue      : %.2f\n", totalRevenue));
        sb.append(divider());
        return sb.toString();
    }

    // ■■ MONTHLY REPORT ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // Shows totals for a given month (YYYY-MM).
    private String monthlyReport(String yearMonth) {
        Table sales = database.get("sales");
        if (sales == null)
            return "Sales table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" MONTHLY REPORT — " + yearMonth + "\n");
        sb.append(divider());

        double revenue = 0;
        int count = 0;
        Map<String, Double> productTotals = new HashMap<>();

        for (Row row : sales.getRows()) {
            String date = row.getValue("date");
            if (date.startsWith(yearMonth)) {
                double total = toDouble(row.getValue("total"));
                revenue += total;
                count++;
                String pid = row.getValue("product_id");
                productTotals.merge(pid, total, Double::sum);
            }
        }

        sb.append(String.format(
            " Total Transactions : %d\n", count));
        sb.append(String.format(
            " Total Revenue      : %.2f\n", revenue));

        if (!productTotals.isEmpty()) {
            sb.append("\n Top Product by Revenue:\n");
            productTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> sb.append(String.format(
                    " Product %-5s : %.2f\n",
                    e.getKey(), e.getValue())));
        }

        sb.append(divider());
        return sb.toString();
    }

    // ■■ LOW STOCK REPORT ■■■■■■■■■■■■■■■■■■■■■■■■■■■
    // Lists products where quantity < threshold.
    private String lowStockReport(int threshold) {
        Table products = database.get("products");
        if (products == null)
            return "Products table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" LOW STOCK ALERT (threshold: " +
            threshold + ")\n");
        sb.append(divider());

        int count = 0;
        for (Row row : products.getRows()) {
            int qty = parseIntSafe(row.getValue("quantity"), 0);
            if (qty < threshold) {
                sb.append(String.format(
                    " %-5s %-20s Qty: %d REORDER NOW\n",
                    row.getValue("id"),
                    row.getValue("name"), qty));
                count++;
            }
        }

        if (count == 0)
            sb.append(" All products above threshold.\n");

        sb.append(divider());
        sb.append(" " + count + " product(s) need reorder.\n");
        sb.append(divider());
        return sb.toString();
    }

    // ■■ STOCK VALUE REPORT ■■■■■■■■■■■■■■■■■■■■■■■■■
    // Total inventory value = sum(price * quantity).
    private String stockValueReport() {
        Table products = database.get("products");
        if (products == null)
            return "Products table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" STOCK VALUATION REPORT\n");
        sb.append(divider());
        sb.append(String.format(
            " %-5s %-20s %8s %8s %12s\n",
            "ID","Name","Price","Qty","Value"));
        sb.append(divider());

        double grandTotal = 0;
        for (Row row : products.getRows()) {
            double price = toDouble(row.getValue("price"));
            int qty = parseIntSafe(row.getValue("quantity"), 0);
            double value = price * qty;
            grandTotal += value;
            sb.append(String.format(
                " %-5s %-20s %8.2f %8d %12.2f\n",
                row.getValue("id"),
                row.getValue("name"),
                price, qty, value));
        }

        sb.append(divider());
        sb.append(String.format(
            " TOTAL STOCK VALUE : %.2f\n", grandTotal));
        sb.append(divider());
        return sb.toString();
    }

    // ■■ TOP PRODUCTS REPORT ■■■■■■■■■■■■■■■■■■■■■■■■
    // Top N products by total quantity sold.
    private String topProductsReport(int n) {
        Table sales = database.get("sales");
        if (sales == null)
            return "Sales table not found.";

        // Sum qty sold per product
        Map<String, Integer> qtySold = new HashMap<>();
        for (Row row : sales.getRows()) {
            String pid = row.getValue("product_id");
            int qty = parseIntSafe(row.getValue("qty"), 0);
            qtySold.merge(pid, qty, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" TOP " + n + " PRODUCTS BY SALES\n");
        sb.append(divider());

        int rank = 1;
        for (Map.Entry<String, Integer> e :
             qtySold.entrySet().stream()
                 .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                 .limit(n)
                 .collect(java.util.stream.Collectors.toList())) {
            // Look up product name
            String name = getProductName(e.getKey());
            sb.append(String.format(
                " #%d %-20s Sold: %d units\n",
                rank++, name, e.getValue()));
        }

        sb.append(divider());
        return sb.toString();
    }

    // ■■ CUSTOMER HISTORY REPORT ■■■■■■■■■■■■■■■■■■■■
    private String customerHistoryReport(String customerId) {
        Table sales = database.get("sales");
        if (sales == null)
            return "Sales table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" CUSTOMER HISTORY — ID: " + customerId + "\n");
        sb.append(divider());

        double totalSpent = 0;
        int purchases = 0;

        for (Row row : sales.getRows()) {
            if (row.getValue("customer_id").equals(customerId)) {
                sb.append(String.format(
                    " Date:%-12s Product:%-5s Qty:%-4s Total:%-8s\n",
                    row.getValue("date"),
                    row.getValue("product_id"),
                    row.getValue("qty"),
                    row.getValue("total")));
                totalSpent += toDouble(row.getValue("total"));
                purchases++;
            }
        }

        if (purchases == 0)
            sb.append(" No purchases found.\n");

        sb.append(divider());
        sb.append(String.format(
            " Total Purchases : %d\n", purchases));
        sb.append(String.format(
            " Total Spent     : %.2f\n", totalSpent));
        sb.append(divider());
        return sb.toString();
    }

    // ■■ TOP CUSTOMERS REPORT ■■■■■■■■■■■■■■■■■■■■■■■
    private String topCustomersReport(int n) {
        Table sales = database.get("sales");
        if (sales == null)
            return "Sales table not found.";

        Map<String, Double> spent = new HashMap<>();
        for (Row row : sales.getRows()) {
            String cid = row.getValue("customer_id");
            double total = toDouble(row.getValue("total"));
            spent.merge(cid, total, Double::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" TOP " + n + " CUSTOMERS\n");
        sb.append(divider());

        int rank = 1;
        for (Map.Entry<String, Double> e :
             spent.entrySet().stream()
                 .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                 .limit(n)
                 .collect(java.util.stream.Collectors.toList())) {
            String name = getCustomerName(e.getKey());
            sb.append(String.format(
                " #%d %-20s Spent: %.2f\n",
                rank++, name, e.getValue()));
        }

        sb.append(divider());
        return sb.toString();
    }

    // ■■ REVENUE REPORT ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private String revenueReport() {
        Table sales = database.get("sales");
        if (sales == null)
            return "Sales table not found.";

        double total = 0;
        int count = 0;
        for (Row row : sales.getRows()) {
            total += toDouble(row.getValue("total"));
            count++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" REVENUE SUMMARY\n");
        sb.append(divider());
        sb.append(String.format(
            " Total Sales : %d\n", count));
        sb.append(String.format(
            " Total Revenue : %.2f\n", total));
        sb.append(String.format(
            " Average Sale Value : %.2f\n",
            count > 0 ? total/count : 0));
        sb.append(divider());
        return sb.toString();
    }

    // ■■ INVENTORY REPORT ■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private String inventoryReport() {
        Table products = database.get("products");
        if (products == null)
            return "Products table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(divider());
        sb.append(" FULL INVENTORY REPORT\n");
        sb.append(divider());
        sb.append(String.format(
            " %-5s %-20s %-10s %-10s\n",
            "ID","Name","Price","Quantity"));
        sb.append(divider());

        for (Row row : products.getRows()) {
            sb.append(String.format(
                " %-5s %-20s %-10s %-10s\n",
                row.getValue("id"),
                row.getValue("name"),
                row.getValue("price"),
                row.getValue("quantity")));
        }

        sb.append(divider());
        sb.append(" Total Products: " +
            products.getRows().size() + "\n");
        sb.append(divider());
        return sb.toString();
    }

    // ■■ HELPERS ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private String getProductName(String id) {
        Table products = database.get("products");
        if (products == null) return "Product-" + id;
        for (Row r : products.getRows())
            if (r.getValue("id").equals(id))
                return r.getValue("name");
        return "Product-" + id;
    }

    private String getCustomerName(String id) {
        Table customers = database.get("customers");
        if (customers == null) return "Customer-" + id;
        for (Row r : customers.getRows())
            if (r.getValue("id").equals(id))
                return r.getValue("name");
        return "Customer-" + id;
    }

    private double toDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private String divider() {
        return " " + "■".repeat(58) + "\n";
    }
}
