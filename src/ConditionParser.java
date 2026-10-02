import java.util.*;

public class ConditionParser {
    // Parse condition string into list of simple conditions
    // Supports: AND, OR, =, !=, >, <, >=, <=, LIKE
    public static boolean evaluate(Row row, String condition) {
        if (condition == null || condition.trim().isEmpty())
            return true;
        String upper = condition.toUpperCase();
        
        // Handle OR conditions first (lower precedence)
        if (upper.contains(" OR ")) {
            String[] parts = condition.split("(?i) OR ");
            for (String part : parts) {
                if (evaluate(row, part.trim()))
                    return true;
            }
            return false;
        }
        
        // Handle AND conditions
        if (upper.contains(" AND ")) {
            String[] parts = condition.split("(?i) AND ");
            for (String part : parts) {
                if (!evaluate(row, part.trim()))
                    return false;
            }
            return true;
        }
        
        // Single condition — detect operator
        return evaluateSingle(row, condition.trim());
    }

    private static boolean evaluateSingle(Row row, String condition) {
        // LIKE operator
        if (condition.toUpperCase().contains(" LIKE ")) {
            String[] p = condition.split("(?i) LIKE ");
            String col = p[0].trim();
            String pattern = p[1].trim();
            
            // Strip quotes around the pattern if present
            if (pattern.startsWith("'") && pattern.endsWith("'") && pattern.length() >= 2) {
                pattern = pattern.substring(1, pattern.length() - 1);
            } else if (pattern.startsWith("\"") && pattern.endsWith("\"") && pattern.length() >= 2) {
                pattern = pattern.substring(1, pattern.length() - 1);
            }
            
            String patternRegex = pattern.replace("%", ".*").replace("_", ".");
            String val = row.getValue(col).toLowerCase();
            return val.matches(".*" + patternRegex.toLowerCase() + ".*");
        }
        
        // >= operator
        if (condition.contains(">=")) {
            String[] p = condition.split(">=");
            double a = toDouble(row.getValue(p[0].trim()));
            double b = toDouble(p[1].trim());
            return a >= b;
        }
        
        // <= operator
        if (condition.contains("<=")) {
            String[] p = condition.split("<=");
            double a = toDouble(row.getValue(p[0].trim()));
            double b = toDouble(p[1].trim());
            return a <= b;
        }
        
        // != operator
        if (condition.contains("!=")) {
            String[] p = condition.split("!=");
            String val1 = row.getValue(p[0].trim());
            String val2 = p[1].trim();
            // strip quotes
            if (val2.startsWith("'") && val2.endsWith("'") && val2.length() >= 2) {
                val2 = val2.substring(1, val2.length() - 1);
            } else if (val2.startsWith("\"") && val2.endsWith("\"") && val2.length() >= 2) {
                val2 = val2.substring(1, val2.length() - 1);
            }
            return !val1.equals(val2);
        }
        
        // > operator
        if (condition.contains(">")) {
            String[] p = condition.split(">");
            double a = toDouble(row.getValue(p[0].trim()));
            double b = toDouble(p[1].trim());
            return a > b;
        }
        
        // < operator
        if (condition.contains("<")) {
            String[] p = condition.split("<");
            double a = toDouble(row.getValue(p[0].trim()));
            double b = toDouble(p[1].trim());
            return a < b;
        }
        
        // = operator (default)
        if (condition.contains("=")) {
            String[] p = condition.split("=");
            String val1 = row.getValue(p[0].trim());
            String val2 = p[1].trim();
            // strip quotes
            if (val2.startsWith("'") && val2.endsWith("'") && val2.length() >= 2) {
                val2 = val2.substring(1, val2.length() - 1);
            } else if (val2.startsWith("\"") && val2.endsWith("\"") && val2.length() >= 2) {
                val2 = val2.substring(1, val2.length() - 1);
            }
            return val1.equals(val2);
        }
        
        return true;
    }

    private static double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
