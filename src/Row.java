import java.util.*;

public class Row {
    private LinkedHashMap<String, String> data;

    public Row() {
        data = new LinkedHashMap<>();
    }

    public void setValue(String column, String value) {
        data.put(column.trim(), value.trim());
    }

    public String getValue(String column) {
        return data.getOrDefault(column.trim(), "");
    }

    public LinkedHashMap<String, String> getData() {
        return data;
    }

    public boolean matchesCondition(String condition) {
        return ConditionParser.evaluate(this, condition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append(entry.getValue()).append(" | ");
        }
        return sb.toString();
    }
}
