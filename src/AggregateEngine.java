import java.util.*;

public class AggregateEngine {
    public static String compute(
        String function,
        String column,
        List<Row> rows) {
        
        switch (function.toUpperCase()) {
            case "COUNT":
                return String.valueOf(rows.size());
            case "SUM": {
                double sum = 0;
                for (Row r : rows)
                    sum += toDouble(r.getValue(column));
                return String.valueOf(sum);
            }
            case "AVG": {
                if (rows.isEmpty()) return "0";
                double sum = 0;
                for (Row r : rows)
                    sum += toDouble(r.getValue(column));
                return String.valueOf(sum / rows.size());
            }
            case "MAX": {
                if (rows.isEmpty()) return "0";
                double max = -Double.MAX_VALUE;
                for (Row r : rows) {
                    max = Math.max(max, toDouble(r.getValue(column)));
                }
                return String.valueOf(max);
            }
            case "MIN": {
                if (rows.isEmpty()) return "0";
                double min = Double.MAX_VALUE;
                for (Row r : rows) {
                    min = Math.min(min, toDouble(r.getValue(column)));
                }
                return String.valueOf(min);
            }
            default:
                return "Unknown function: " + function;
        }
    }

    private static double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
