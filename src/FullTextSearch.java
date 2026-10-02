import java.util.*;

/**
 * FullTextSearch — searches for a keyword across ALL columns
 * of every row in a table. Does not require an index.
 * Usage: SEARCH products Rice
 */
public class FullTextSearch {
    // Returns all rows where ANY column contains keyword.
    public static List<Row> search(Table table, String keyword) {
        List<Row> results = new ArrayList<>();
        if (keyword == null || keyword.isEmpty())
            return results;
        String kw = keyword.toLowerCase().trim();
        for (Row row : table.getRows()) {
            boolean found = false;
            for (String val : row.getData().values()) {
                if (val.toLowerCase().contains(kw)) {
                    found = true;
                    break;
                }
            }
            if (found) results.add(row);
        }
        System.out.println("FullTextSearch: found " + results.size() + " row(s) for keyword: " + keyword);
        return results;
    }

    // Returns rows where keyword found in specified columns.
    public static List<Row> searchInColumns(Table table, String keyword, List<String> columns) {
        List<Row> results = new ArrayList<>();
        String kw = keyword.toLowerCase().trim();
        for (Row row : table.getRows()) {
            for (String col : columns) {
                String val = row.getValue(col).toLowerCase();
                if (val.contains(kw)) {
                    results.add(row);
                    break;
                }
            }
        }
        return results;
    }

    public static Map<String, List<Row>> searchAllTables(Map<String, Table> database, String keyword) {
        Map<String, List<Row>> results = new HashMap<>();
        for (Map.Entry<String, Table> entry : database.entrySet()) {
            List<Row> found = search(entry.getValue(), keyword);
            if (!found.isEmpty())
                results.put(entry.getKey(), found);
        }
        return results;
    }
}
