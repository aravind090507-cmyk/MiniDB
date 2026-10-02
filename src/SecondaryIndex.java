import java.util.*;

/**
 * SecondaryIndex — maps one column value to MULTIPLE Rows.
 * Best for non-unique columns: category, price, date, status.
 * e.g. category=Food -> [Row(Rice), Row(Milk), Row(Oil)]
 */
public class SecondaryIndex {
    private HashMap<String, List<Row>> index;
    private String columnName;

    public SecondaryIndex(String columnName) {
        this.columnName = columnName;
        this.index = new HashMap<>();
    }

    public void build(List<Row> rows) {
        index.clear();
        for (Row row : rows)
            add(row);
        System.out.println("SecondaryIndex built on [" + columnName + "] — " + index.size() + " keys.");
    }

    public void add(Row row) {
        String key = row.getValue(columnName);
        if (key == null || key.isEmpty()) return;
        // computeIfAbsent creates new list if key missing
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
    }

    // Returns all rows where columnName = value.
    public List<Row> find(String value) {
        return index.getOrDefault(value, new ArrayList<>());
    }

    public void remove(Row row) {
        String key = row.getValue(columnName);
        List<Row> list = index.get(key);
        if (list != null) {
            list.remove(row);
            if (list.isEmpty())
                index.remove(key);
        }
    }

    public void rebuildEntry(String oldKey, Row row) {
        // Remove from old bucket
        List<Row> oldList = index.get(oldKey);
        if (oldList != null) oldList.remove(row);
        // Add to new bucket
        add(row);
    }

    public String getColumnName() { return columnName; }
    public Set<String> getAllKeys() { return index.keySet(); }
    public int keyCount() { return index.size(); }

    // Count how many rows have a given value
    public int countByValue(String value) {
        return index.getOrDefault(value, new ArrayList<>()).size();
    }
}
