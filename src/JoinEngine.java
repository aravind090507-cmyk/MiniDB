import java.util.*;

public class JoinEngine {
    // JOIN table1 and table2 on matching column values
    // Example: JOIN sales ON products.id = sales.product_id
    public static List<Row> join(
        Table left,
        Table right,
        String leftCol,
        String rightCol) {
        
        List<Row> result = new ArrayList<>();
        
        // Strip table prefixes if present (e.g. "products.id" -> "id")
        String cleanLeftCol = leftCol.contains(".") 
            ? leftCol.substring(leftCol.indexOf(".") + 1).trim() 
            : leftCol.trim();
        String cleanRightCol = rightCol.contains(".") 
            ? rightCol.substring(rightCol.indexOf(".") + 1).trim() 
            : rightCol.trim();

        for (Row leftRow : left.getRows()) {
            String leftVal = leftRow.getValue(cleanLeftCol);
            for (Row rightRow : right.getRows()) {
                String rightVal = rightRow.getValue(cleanRightCol);
                if (leftVal.equals(rightVal)) {
                    // Merge both rows into one
                    Row merged = new Row();
                    // Add left table columns prefixed with left table name
                    leftRow.getData().forEach((k, v) ->
                        merged.setValue(left.getName() + "." + k, v)
                    );
                    // Add right table columns prefixed with right table name
                    rightRow.getData().forEach((k, v) ->
                        merged.setValue(right.getName() + "." + k, v)
                    );
                    result.add(merged);
                }
            }
        }
        return result;
    }
}
