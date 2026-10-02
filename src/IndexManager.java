import java.util.*;
import java.io.*;

/**
 * IndexManager — central registry for all indexes.
 * Creates, stores, rebuilds and persists indexes to disk.
 * Plugs directly into DBEngine.
 */
public class IndexManager {
    // tableName -> columnName -> PrimaryIndex
    private HashMap<String, HashMap<String, PrimaryIndex>> primaryIdx;

    // tableName -> columnName -> SecondaryIndex
    private HashMap<String, HashMap<String, SecondaryIndex>> secondaryIdx;

    private String indexFolder;

    public IndexManager(String folder) {
        this.indexFolder = folder;
        this.primaryIdx = new HashMap<>();
        this.secondaryIdx = new HashMap<>();
        new File(folder).mkdirs();
    }

    public void createPrimary(Table table, String col) {
        PrimaryIndex idx = new PrimaryIndex(col);
        idx.build(table.getRows());
        primaryIdx.computeIfAbsent(table.getName(), k -> new HashMap<>()).put(col, idx);
        saveMetadata(table.getName());
        System.out.println("Primary index: " + table.getName() + "." + col);
    }

    public void createSecondary(Table table, String col) {
        SecondaryIndex idx = new SecondaryIndex(col);
        idx.build(table.getRows());
        secondaryIdx.computeIfAbsent(table.getName(), k -> new HashMap<>()).put(col, idx);
        saveMetadata(table.getName());
        System.out.println("Secondary index: " + table.getName() + "." + col);
    }

    // Returns null if no index or value not found.
    public Row findByPrimary(String tbl, String col, String val) {
        HashMap<String, PrimaryIndex> tblIdx = primaryIdx.get(tbl);
        if (tblIdx == null) return null;
        PrimaryIndex idx = tblIdx.get(col);
        return (idx == null) ? null : idx.find(val);
    }

    public List<Row> findBySecondary(String tbl, String col, String val) {
        HashMap<String, SecondaryIndex> tblIdx = secondaryIdx.get(tbl);
        if (tblIdx == null) return new ArrayList<>();
        SecondaryIndex idx = tblIdx.get(col);
        return (idx == null) ? new ArrayList<>() : idx.find(val);
    }

    // Must be called after every INSERT/UPDATE/DELETE.
    public void rebuild(Table table) {
        String name = table.getName();
        if (primaryIdx.containsKey(name)) {
            for (PrimaryIndex i : primaryIdx.get(name).values()) {
                i.build(table.getRows());
            }
        }
        if (secondaryIdx.containsKey(name)) {
            for (SecondaryIndex i : secondaryIdx.get(name).values()) {
                i.build(table.getRows());
            }
        }
    }

    public void drop(String tableName) {
        primaryIdx.remove(tableName);
        secondaryIdx.remove(tableName);
        new File(indexFolder + tableName + ".idx").delete();
    }

    public boolean hasPrimary(String tbl, String col) {
        return primaryIdx.containsKey(tbl) && primaryIdx.get(tbl).containsKey(col);
    }

    public boolean hasSecondary(String tbl, String col) {
        return secondaryIdx.containsKey(tbl) && secondaryIdx.get(tbl).containsKey(col);
    }

    private void saveMetadata(String tableName) {
        String path = indexFolder + tableName + ".idx";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            if (primaryIdx.containsKey(tableName)) {
                for (String col : primaryIdx.get(tableName).keySet()) {
                    bw.write("PRIMARY:" + col);
                    bw.newLine();
                }
            }
            if (secondaryIdx.containsKey(tableName)) {
                for (String col : secondaryIdx.get(tableName).keySet()) {
                    bw.write("SECONDARY:" + col);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Index save error: " + e.getMessage());
        }
    }

    public void loadIndexes(Table table) {
        String path = indexFolder + table.getName() + ".idx";
        File f = new File(path);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":");
                if (parts[0].equals("PRIMARY"))
                    createPrimary(table, parts[1]);
                else if (parts[0].equals("SECONDARY"))
                    createSecondary(table, parts[1]);
            }
        } catch (IOException e) {
            System.out.println("Index load error: " + e.getMessage());
        }
    }

    public void printIndexInfo() {
        System.out.println("=== INDEX REGISTRY ===");
        primaryIdx.forEach((tbl, cols) ->
            cols.forEach((col, idx) ->
                System.out.println("PRIMARY " + tbl + "." + col + " [" + idx.size() + " entries]")
            )
        );
        secondaryIdx.forEach((tbl, cols) ->
            cols.forEach((col, idx) ->
                System.out.println("SECONDARY " + tbl + "." + col + " [" + idx.keyCount() + " keys]")
            )
        );
    }
}
