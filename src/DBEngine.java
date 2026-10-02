import java.util.*;

public class DBEngine {
    private HashMap<String, Table> database;
    private StorageManager storage;

    // Managers for Models 6, 7 & 8
    private IndexManager indexManager;
    private TransactionManager txnManager;
    private AuthManager authManager;
    private SessionManager sessionManager;

    // Model 9 & 10 fields
    private ReportEngine reportEngine;
    private ReportExporter reportExporter;
    private ReportScheduler reportScheduler;
    private BackupManager backupManager;
    private BackupScheduler backupScheduler;
    private IntegrityChecker integrityChecker;

    public DBEngine(String dataFolder) {
        database = new HashMap<>();
        storage = new StorageManager(dataFolder);
        loadAllTables();

        // Model 6 Index Manager
        indexManager = new IndexManager("indexes/");
        for (Table t : database.values()) {
            indexManager.loadIndexes(t);
        }

        // Model 7 Transaction Manager
        txnManager = new TransactionManager(this, "transaction.log");

        // Model 8 Authentication & Session Managers
        authManager = new AuthManager("users.csv");
        sessionManager = new SessionManager();

        // Model 9 Reporting & Analytics
        reportEngine = new ReportEngine(database);
        reportExporter = new ReportExporter("reports/");
        reportScheduler = new ReportScheduler(reportEngine, reportExporter);
        reportScheduler.startDailySchedule();

        // Model 10 Backup & Recovery
        backupManager = new BackupManager("data/", "backups/");
        integrityChecker = new IntegrityChecker();
        backupScheduler = new BackupScheduler(backupManager, 7);
        backupScheduler.start();
    }

    private void loadAllTables() {
        for (String name : storage.getAllTableNames()) {
            Table t = storage.loadTable(name);
            if (t != null) database.put(name, t);
        }
        System.out.println("Loaded " + database.size() + " table(s).");
    }

    // New execute with auth check (Model 8)
    public String execute(String input, String sessionId) {
        if (input == null || input.trim().isEmpty())
            return "Empty command.";
        Command cmd = QueryParser.parse(input);

        // Handle error command returned from parser
        if ("ERROR".equals(cmd.getType())) {
            return cmd.getErrorMessage();
        }

        // Handle login specially (no session needed)
        if ("LOGIN".equals(cmd.getType())) {
            return handleLogin(cmd.getUsername(), cmd.getPassword());
        }

        // All other commands need a valid session
        Session session = sessionManager.get(sessionId);
        if (session == null) {
            return "Please login first. Type: LOGIN <username> <password>";
        }

        // Check permission
        if (!session.canExecute(cmd.getType())) {
            return "Permission denied. Your role [" + session.getRole() + "] cannot perform: " + cmd.getType();
        }

        // Route command
        switch (cmd.getType()) {
            case "LOGOUT":
                return sessionManager.logout(sessionId);
            case "CREATE_USER":
                return authManager.createUser(
                    cmd.getUsername(),
                    cmd.getPassword(),
                    cmd.getRole());
            case "CHANGE_ROLE":
                return authManager.changeRole(
                    cmd.getUsername(), cmd.getRole());
            case "DISABLE_USER":
                return authManager.disableUser(
                    cmd.getUsername());
            case "ENABLE_USER":
                return authManager.enableUser(
                    cmd.getUsername());
            case "LIST_USERS":
                authManager.listUsers().forEach(u -> System.out.println(u));
                return "";

            // Transactions routing (Model 7)
            case "BEGIN_TXN":
                return txnManager.begin();
            case "COMMIT_TXN":
                return txnManager.commit(cmd.getTransactionId());
            case "ROLLBACK_TXN":
                return txnManager.rollback(cmd.getTransactionId());
            case "TXN_STMT":
                return txnManager.add(cmd.getTransactionId(), cmd.getPendingSql());
            case "LIST_TXN":
                txnManager.printActive();
                return "";

            // Database operations
            case "CREATE": return executeCreate(cmd);
            case "INSERT": return executeInsert(cmd);
            case "SELECT": return executeSelect(cmd);
            case "UPDATE": return executeUpdate(cmd);
            case "DELETE": return executeDelete(cmd);
            case "DROP": return executeDrop(cmd);
            case "CREATE_INDEX": return executeCreateIndex(cmd);
            case "SEARCH": return executeSearch(cmd);
            case "LIST_INDEXES":
                indexManager.printIndexInfo();
                return "";
            case "SHOW_TABLES":
                StringBuilder sb = new StringBuilder();
                database.keySet().forEach(k -> sb.append(k).append("\n"));
                return sb.toString().trim();

            // Model 9 Reporting
            case "REPORT": return executeReport(cmd);
            case "EXPORT_TABLE": return executeExportTable(cmd);
            case "LIST_REPORTS": return executeListReports();

            // Model 10 Backup & Recovery
            case "BACKUP": return backupManager.createBackup();
            case "RESTORE": {
                String msg = backupManager.restore(cmd.getBackupId());
                if (msg.contains("complete")) {
                    database.clear();
                    loadAllTables();
                }
                return msg;
            }
            case "LIST_BACKUPS": {
                List<String> bkps = backupManager.listBackups();
                if (bkps.isEmpty()) return "No backups found.";
                StringBuilder sbb = new StringBuilder();
                sbb.append("Available Backups (newest first):\n");
                bkps.forEach(b -> sbb.append("  ").append(b).append("\n"));
                return sbb.toString().trim();
            }
            case "CHECK_INTEGRITY":
                return integrityChecker.checkDatabase(database);
            case "PRUNE_BACKUPS": {
                int keep = Integer.parseInt(cmd.getBackupId() != null ? cmd.getBackupId() : "7");
                return backupManager.pruneOldBackups(keep);
            }
            default:
                return "Unknown command: " + input;
        }
    }

    // Bypass execute for commit operations and backwards compatibility
    public String execute(String input) {
        if (input == null || input.trim().isEmpty())
            return "Empty command.";
        Command cmd = QueryParser.parse(input);
        if ("ERROR".equals(cmd.getType())) {
            return cmd.getErrorMessage();
        }

        switch (cmd.getType()) {
            case "CREATE": return executeCreate(cmd);
            case "INSERT": return executeInsert(cmd);
            case "SELECT": return executeSelect(cmd);
            case "UPDATE": return executeUpdate(cmd);
            case "DELETE": return executeDelete(cmd);
            case "DROP": return executeDrop(cmd);
            case "CREATE_INDEX": return executeCreateIndex(cmd);
            case "SEARCH": return executeSearch(cmd);
            case "LIST_INDEXES":
                indexManager.printIndexInfo();
                return "";
            case "SHOW_TABLES":
                StringBuilder sb = new StringBuilder();
                database.keySet().forEach(k -> sb.append(k).append("\n"));
                return sb.toString().trim();

            // Model 9 Reporting
            case "REPORT": return executeReport(cmd);
            case "EXPORT_TABLE": return executeExportTable(cmd);
            case "LIST_REPORTS": return executeListReports();

            // Model 10 Backup & Recovery
            case "BACKUP": return backupManager.createBackup();
            case "RESTORE": {
                String msg = backupManager.restore(cmd.getBackupId());
                if (msg.contains("complete")) {
                    database.clear();
                    loadAllTables();
                }
                return msg;
            }
            case "LIST_BACKUPS": {
                List<String> bkps = backupManager.listBackups();
                if (bkps.isEmpty()) return "No backups found.";
                StringBuilder sbb = new StringBuilder();
                sbb.append("Available Backups (newest first):\n");
                bkps.forEach(b -> sbb.append("  ").append(b).append("\n"));
                return sbb.toString().trim();
            }
            case "CHECK_INTEGRITY":
                return integrityChecker.checkDatabase(database);
            case "PRUNE_BACKUPS": {
                int keep = Integer.parseInt(cmd.getBackupId() != null ? cmd.getBackupId() : "7");
                return backupManager.pruneOldBackups(keep);
            }
            default:
                return "Unknown command: " + input;
        }
    }

    private String handleLogin(String username, String password) {
        Session session = authManager.login(username, password);
        if (session == null) {
            return "Login failed. Check username/password.";
        }
        sessionManager.add(session);
        return "Login successful! Session: " + session.getSessionId() + " Role: " + session.getRole();
    }

    private String executeCreate(Command cmd) {
        String name = cmd.getTableName();
        if (database.containsKey(name))
            return "Table already exists: " + name;
        Table table = new Table(name, cmd.getColumns());
        database.put(name, table);
        storage.saveTable(table);
        return "Table created: " + name;
    }

    private String executeInsert(Command cmd) {
        Table table = database.get(cmd.getTableName());
        if (table == null) return "Table not found.";
        List<String> schema = table.getSchema();
        List<String> values = cmd.getValues();
        if (values.size() != schema.size())
            return "Column count mismatch.";
        Row row = new Row();
        for (int i = 0; i < schema.size(); i++)
            row.setValue(schema.get(i), values.get(i));
        table.addRow(row);
        storage.saveTable(table);
        indexManager.rebuild(table); // Rebuild indexes
        return "1 row inserted.";
    }

    private String executeSelect(Command cmd) {
        String name = cmd.getTableName();
        Table table = database.get(name);
        if (table == null)
            return "Table not found: " + name;

        String condition = cmd.getCondition();
        List<Row> results = null;
        boolean usedIndex = false;

        // Try index if condition is simple equality and no JOIN is performed
        if (condition != null &&
            condition.contains("=") &&
            !condition.contains("!=") &&
            !condition.contains(">") &&
            !condition.contains("<") &&
            !condition.toUpperCase().contains(" AND ") &&
            !condition.toUpperCase().contains(" OR ") &&
            cmd.getJoinTable() == null) {

            String[] parts = condition.split("=", 2);
            String col = parts[0].trim();
            String val = parts[1].trim();

            // Try primary index first (single row)
            Row r = indexManager.findByPrimary(name, col, val);
            if (r != null) {
                results = new ArrayList<>(Collections.singletonList(r));
                System.out.println("[Used PRIMARY index]");
                usedIndex = true;
            } else {
                // Try secondary index (multiple rows)
                List<Row> rows = indexManager.findBySecondary(name, col, val);
                if (!rows.isEmpty()) {
                    results = new ArrayList<>(rows);
                    System.out.println("[Used SECONDARY index]");
                    usedIndex = true;
                }
            }
        }

        if (!usedIndex) {
            // Check for aggregate function
            List<String> cols = cmd.getColumns();
            if (cols != null && cols.size() == 1) {
                String col = cols.get(0).trim();
                if (col.toUpperCase().startsWith("COUNT") ||
                    col.toUpperCase().startsWith("SUM") ||
                    col.toUpperCase().startsWith("AVG") ||
                    col.toUpperCase().startsWith("MAX") ||
                    col.toUpperCase().startsWith("MIN")) {
                    return handleAggregate(cmd, table);
                }
            }

            // Handle JOIN
            List<String> columnsList = new ArrayList<>();
            if (cmd.getJoinTable() != null) {
                Table joinTable = database.get(cmd.getJoinTable());
                if (joinTable == null)
                    return "Join table not found: " + cmd.getJoinTable();
                String[] on = cmd.getJoinOn().split("=");
                results = JoinEngine.join(table, joinTable, on[0].trim(), on[1].trim());

                // Build joined schema columns
                for (String c : table.getSchema()) {
                    columnsList.add(table.getName() + "." + c);
                }
                for (String c : joinTable.getSchema()) {
                    columnsList.add(joinTable.getName() + "." + c);
                }
            } else {
                results = new ArrayList<>();
                for (Row row : table.getRows()) {
                    if (ConditionParser.evaluate(row, cmd.getCondition()))
                        results.add(row);
                }
                // Columns list
                if (cols != null && !cols.isEmpty()) {
                    columnsList.addAll(cols);
                } else {
                    columnsList.addAll(table.getSchema());
                }
            }

            // Apply ORDER BY
            if (cmd.getOrderBy() != null) {
                String[] parts = cmd.getOrderBy().split("\\s+");
                String colName = parts[0].trim();
                boolean asc = parts.length < 2 || parts[1].equalsIgnoreCase("ASC");
                results = SortEngine.sort(results, colName, asc);
            }

            // Apply LIMIT
            if (cmd.getLimit() > -1 && results.size() > cmd.getLimit()) {
                results = results.subList(0, cmd.getLimit());
            }

            printCustomTable(columnsList, results);
            return "";
        } else {
            // Apply custom column filtering / order / limit if they are present on index result
            List<String> columnsList = new ArrayList<>();
            List<String> cols = cmd.getColumns();
            if (cols != null && !cols.isEmpty()) {
                columnsList.addAll(cols);
            } else {
                columnsList.addAll(table.getSchema());
            }

            if (cmd.getOrderBy() != null) {
                String[] parts = cmd.getOrderBy().split("\\s+");
                String colName = parts[0].trim();
                boolean asc = parts.length < 2 || parts[1].equalsIgnoreCase("ASC");
                results = SortEngine.sort(results, colName, asc);
            }

            if (cmd.getLimit() > -1 && results.size() > cmd.getLimit()) {
                results = results.subList(0, cmd.getLimit());
            }

            printCustomTable(columnsList, results);
            return "";
        }
    }

    private String handleAggregate(Command cmd, Table table) {
        String col = cmd.getColumns().get(0).trim();
        String func = col.substring(0, col.indexOf("(")).trim();
        String colName = col.substring(col.indexOf("(") + 1, col.indexOf(")")).trim();
        List<Row> rows;
        if (cmd.getCondition() != null) {
            rows = new ArrayList<>();
            for (Row r : table.getRows()) {
                if (ConditionParser.evaluate(r, cmd.getCondition()))
                    rows.add(r);
            }
        } else {
            rows = table.getRows();
        }
        String result = AggregateEngine.compute(func, colName, rows);
        System.out.println(func + "(" + colName + ") = " + result);
        return "";
    }

    private void printCustomTable(List<String> schema, List<Row> rows) {
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

    private String executeUpdate(Command cmd) {
        Table table = database.get(cmd.getTableName());
        if (table == null) return "Table not found.";
        int count = table.updateRows(
            cmd.getSetClause(), cmd.getCondition());
        storage.saveTable(table);
        indexManager.rebuild(table); // Rebuild indexes
        return count + " row(s) updated.";
    }

    private String executeDelete(Command cmd) {
        Table table = database.get(cmd.getTableName());
        if (table == null) return "Table not found.";
        int before = table.getRows().size();
        table.removeRow(cmd.getCondition());
        int deleted = before - table.getRows().size();
        storage.saveTable(table);
        indexManager.rebuild(table); // Rebuild indexes
        return deleted + " row(s) deleted.";
    }

    private String executeDrop(Command cmd) {
        String name = cmd.getTableName();
        if (!database.containsKey(name))
            return "Table not found.";
        database.remove(name);
        storage.deleteTableFile(name);
        indexManager.drop(name); // Drop index
        return "Table dropped: " + name;
    }

    private String executeCreateIndex(Command cmd) {
        String tbl = cmd.getTableName();
        Table table = database.get(tbl);
        if (table == null)
            return "Table not found: " + tbl;
        String col = cmd.getColumns().get(0);
        if ("SECONDARY".equals(cmd.getIndexType())) {
            indexManager.createSecondary(table, col);
            return "Secondary index created on " + tbl + "." + col;
        } else {
            indexManager.createPrimary(table, col);
            return "Primary index created on " + tbl + "." + col;
        }
    }

    private String executeSearch(Command cmd) {
        Table table = database.get(cmd.getTableName());
        if (table == null)
            return "Table not found: " + cmd.getTableName();
        List<Row> results = FullTextSearch.search(table, cmd.getSearchKeyword());
        table.printTable(results);
        return "";
    }

    public HashMap<String, Table> getDatabase() {
        return database;
    }

    // ■■ NEW executeReport() ■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private String executeReport(Command cmd) {
        String type = cmd.getReportType();
        String param = cmd.getReportParam();
        boolean export = cmd.isExport();
        String content = reportEngine.generate(type, param);
        System.out.println(content);
        if (export)
            return reportExporter.exportText(content, type);
        return "";
    }

    private String executeExportTable(Command cmd) {
        Table table = database.get(cmd.getTableName());
        if (table == null)
            return "Table not found: " + cmd.getTableName();
        return reportExporter.exportTableCSV(table);
    }

    private String executeListReports() {
        List<String> files = reportExporter.listExports();
        if (files.isEmpty()) return "No reports found.";
        StringBuilder sb = new StringBuilder();
        sb.append("Saved Reports:\n");
        files.forEach(f -> sb.append("  ").append(f).append("\n"));
        return sb.toString();
    }
}
