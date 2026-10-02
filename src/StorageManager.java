import java.io.*;
import java.util.*;

public class StorageManager {
    private String dataFolder;

    public StorageManager(String folder) {
        this.dataFolder = folder;
        new File(folder).mkdirs();
    }

    public void saveTable(Table table) {
        String path = dataFolder + table.getName() + ".csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(String.join(",", table.getSchema()));
            bw.newLine();
            for (Row row : table.getRows()) {
                List<String> values = new ArrayList<>();
                for (String col : table.getSchema())
                    values.add(row.getValue(col));
                bw.write(String.join(",", values));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    public Table loadTable(String tableName) {
        String path = dataFolder + tableName + ".csv";
        File file = new File(path);
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null) return null;
            String[] cols = headerLine.split(",");
            List<String> schema = new ArrayList<>();
            for (String col : cols) schema.add(col.trim());
            Table table = new Table(tableName, schema);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] vals = line.split(",", -1);
                Row row = new Row();
                for (int i = 0; i < schema.size(); i++) {
                    String val = (i < vals.length)
                        ? vals[i].trim() : "";
                    row.setValue(schema.get(i), val);
                }
                table.addRow(row);
            }
            return table;
        } catch (IOException e) {
            System.out.println("Error loading: " + e.getMessage());
            return null;
        }
    }

    public void deleteTableFile(String tableName) {
        new File(dataFolder + tableName + ".csv").delete();
    }

    public List<String> getAllTableNames() {
        List<String> names = new ArrayList<>();
        File[] files = new File(dataFolder)
            .listFiles((d, n) -> n.endsWith(".csv"));
        if (files != null)
            for (File f : files)
                names.add(f.getName().replace(".csv",""));
        return names;
    }
}
