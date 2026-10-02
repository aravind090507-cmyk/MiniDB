import java.util.*;

public class Table {
    private String name;
    private List<String> schema;
    private List<Row> rows;

    public Table(String name, List<String> schema) {
        this.name = name;
        this.schema = schema;
        this.rows = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<String> getSchema() { return schema; }
    public List<Row> getRows() { return rows; }
    public void addRow(Row row) { rows.add(row); }

    public boolean removeRow(String condition) {
        int before = rows.size();
        rows.removeIf(r -> r.matchesCondition(condition));
        return rows.size() < before;
    }

    public int updateRows(String setClause, String condition) {
        int count = 0;
        String[] setParts = setClause.split("=");
        String col = setParts[0].trim();
        String val = setParts[1].trim();
        for (Row row : rows) {
            if (row.matchesCondition(condition)) {
                row.setValue(col, val);
                count++;
            }
        }
        return count;
    }

    public List<Row> selectRows(String condition) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows)
            if (row.matchesCondition(condition))
                result.add(row);
        return result;
    }

    public void printTable(List<Row> rows) {
        System.out.println();
        System.out.print("+");
        for (String col : schema)
            System.out.printf("-%-12s+", "------------");
        System.out.println();
        System.out.print("|");
        for (String col : schema)
            System.out.printf(" %-12s|", col);
        System.out.println();
        System.out.print("+");
        for (String col : schema)
            System.out.printf("-%-12s+", "------------");
        System.out.println();
        for (Row row : rows) {
            System.out.print("|");
            for (String col : schema)
                System.out.printf(" %-12s|", row.getValue(col));
            System.out.println();
        }
        System.out.print("+");
        for (String col : schema)
            System.out.printf("-%-12s+", "------------");
        System.out.println();
        System.out.println(rows.size() + " row(s) found.");
    }
}
