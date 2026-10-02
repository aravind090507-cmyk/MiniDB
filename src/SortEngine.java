import java.util.*;

public class SortEngine {
    // Sort rows by column name, ASC or DESC
    public static List<Row> sort(
        List<Row> rows,
        String column,
        boolean ascending) {
        
        rows.sort((a, b) -> {
            String valA = a.getValue(column);
            String valB = b.getValue(column);
            
            // Try numeric comparison first
            try {
                double numA = Double.parseDouble(valA);
                double numB = Double.parseDouble(valB);
                int cmp = Double.compare(numA, numB);
                return ascending ? cmp : -cmp;
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                int cmp = valA.compareTo(valB);
                return ascending ? cmp : -cmp;
            }
        });
        return rows;
    }
}
