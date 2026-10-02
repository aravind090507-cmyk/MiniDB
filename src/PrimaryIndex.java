import java.util.*;

/**
 * PrimaryIndex — maps one column value to exactly one Row.
 * Best for unique columns like id, order_number, email.
 * Lookup time: O(1) regardless of table size.
 */
public class PrimaryIndex {
    private HashMap<String, Row> index;
    private String columnName;

    public PrimaryIndex(String columnName) {
        this.columnName = columnName;
        this.index = new HashMap<>();
    }

    // Build index from scratch using all table rows.
    // Called on startup or after table reload.
    public void build(List<Row> rows) {
        index.clear();
        for (Row row : rows) {
            String key = row.getValue(columnName);
            if (key != null && !key.isEmpty())
                index.put(key, row);
        }
        System.out.println("PrimaryIndex built on [" + columnName + "] — " + index.size() + " entries.");
    }

    // Returns the Row matching the value, or null.
    public Row find(String value) {
        return index.get(value);
    }

    // Called after INSERT to keep index in sync.
    public void add(Row row) {
        String key = row.getValue(columnName);
        if (key != null && !key.isEmpty())
            index.put(key, row);
    }

    // Called after DELETE to remove from index.
    public void remove(String value) {
        index.remove(value);
    }

    // Called after UPDATE — remove old key, add new.
    public void update(String oldVal, String newVal, Row row) {
        index.remove(oldVal);
        if (newVal != null && !newVal.isEmpty())
            index.put(newVal, row);
    }

    public String getColumnName() { return columnName; }
    public int size() { return index.size(); }
    public boolean contains(String value) {
        return index.containsKey(value);
    }
    public Set<String> getAllKeys() {
        return index.keySet();
    }
}
