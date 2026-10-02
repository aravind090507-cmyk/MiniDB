import java.util.*;

public class QueryParser {
    public static Command parse(String input) {
        input = input.trim();
        String upper = input.toUpperCase();
        Command cmd = new Command();

        // 1. Check Auth commands first
        if (upper.startsWith("LOGIN")) {
            cmd.setType("LOGIN");
            String[] p = input.split("\\s+");
            if (p.length < 3)
                return errorCmd("Usage: LOGIN <user> <pass>");
            cmd.setUsername(p[1].trim());
            cmd.setPassword(p[2].trim());
            return cmd;
        }
        if (upper.equals("LOGOUT")) {
            cmd.setType("LOGOUT");
            return cmd;
        }
        if (upper.startsWith("CREATE USER")) {
            cmd.setType("CREATE_USER");
            String[] p = input.split("\\s+");
            if (p.length < 5)
                return errorCmd("Usage: CREATE USER <u> <pass> <role>");
            cmd.setUsername(p[2].trim());
            cmd.setPassword(p[3].trim());
            cmd.setRole(p[4].trim());
            return cmd;
        }
        if (upper.equals("LIST USERS")) {
            cmd.setType("LIST_USERS");
            return cmd;
        }
        if (upper.startsWith("CHANGE ROLE")) {
            cmd.setType("CHANGE_ROLE");
            String[] p = input.split("\\s+");
            if (p.length < 4)
                return errorCmd("Usage: CHANGE ROLE <user> <role>");
            cmd.setUsername(p[2].trim());
            cmd.setRole(p[3].trim());
            return cmd;
        }
        if (upper.startsWith("DISABLE USER")) {
            cmd.setType("DISABLE_USER");
            String[] p = input.split("\\s+");
            if (p.length < 3)
                return errorCmd("Usage: DISABLE USER <user>");
            cmd.setUsername(p[2].trim());
            return cmd;
        }
        if (upper.startsWith("ENABLE USER")) {
            cmd.setType("ENABLE_USER");
            String[] p = input.split("\\s+");
            if (p.length < 3)
                return errorCmd("Usage: ENABLE USER <user>");
            cmd.setUsername(p[2].trim());
            return cmd;
        }

        // 2. Check Transaction commands
        if (upper.equals("BEGIN") || upper.equals("BEGIN TRANSACTION")) {
            cmd.setType("BEGIN_TXN");
            return cmd;
        }
        if (upper.startsWith("COMMIT")) {
            cmd.setType("COMMIT_TXN");
            String[] p = input.split("\\s+");
            if (p.length > 1) cmd.setTransactionId(p[1].trim());
            return cmd;
        }
        if (upper.startsWith("ROLLBACK")) {
            cmd.setType("ROLLBACK_TXN");
            String[] p = input.split("\\s+");
            if (p.length > 1) cmd.setTransactionId(p[1].trim());
            return cmd;
        }
        if (upper.equals("LIST TRANSACTIONS") || upper.equals("LIST TXN")) {
            cmd.setType("LIST_TXN");
            return cmd;
        }
        if (upper.startsWith("TXN-")) {
            cmd.setType("TXN_STMT");
            int space = input.indexOf(" ");
            if (space > -1) {
                cmd.setTransactionId(input.substring(0, space).trim());
                cmd.setPendingSql(input.substring(space).trim());
            } else {
                return errorCmd("Invalid TXN format");
            }
            return cmd;
        }

        // 3. Check Index command
        if (upper.startsWith("CREATE INDEX")) {
            parseCreateIndex(input, cmd);
            return cmd;
        }
        if (upper.equals("LIST INDEXES") || upper.equals("SHOW INDEXES")) {
            cmd.setType("LIST_INDEXES");
            return cmd;
        }

        // 4. Check Full text search
        if (upper.startsWith("SEARCH")) {
            cmd.setType("SEARCH");
            String[] p = input.split("\\s+");
            if (p.length < 3) {
                return errorCmd("Usage: SEARCH <tableName> <keyword>");
            }
            cmd.setTableName(p[1].trim());
            cmd.setSearchKeyword(p[2].trim());
            return cmd;
        }
        if (upper.equals("SHOW TABLES")) {
            cmd.setType("SHOW_TABLES");
            return cmd;
        }

        // Model 9 Reporting
        // REPORT DAILY_SALES 2024-01-15
        // REPORT LOW_STOCK 10
        // REPORT TOP_PRODUCTS 5 EXPORT
        if (upper.startsWith("REPORT")) {
            cmd.setType("REPORT");
            String[] parts = input.trim().split("\\s+");
            if (parts.length < 2)
                return errorCmd("Usage: REPORT <type> [param] [EXPORT]");
            cmd.setReportType(parts[1].toUpperCase());
            if (parts.length >= 3 && !parts[2].equalsIgnoreCase("EXPORT"))
                cmd.setReportParam(parts[2].trim());
            cmd.setExport(input.toUpperCase().endsWith("EXPORT"));
            return cmd;
        }

        // EXPORT TABLE products
        if (upper.startsWith("EXPORT TABLE")) {
            cmd.setType("EXPORT_TABLE");
            String[] parts = input.trim().split("\\s+");
            cmd.setTableName(parts[2].trim());
            return cmd;
        }

        // LIST REPORTS
        if (upper.equals("LIST REPORTS")) {
            cmd.setType("LIST_REPORTS");
            return cmd;
        }

        // Model 10 Backup & Recovery
        // BACKUP
        if (upper.equals("BACKUP")) {
            cmd.setType("BACKUP");
            return cmd;
        }

        // RESTORE backup_20240115_090000
        if (upper.startsWith("RESTORE")) {
            cmd.setType("RESTORE");
            String[] p = input.trim().split("\\s+");
            if (p.length < 2)
                return errorCmd("Usage: RESTORE <backup_id>");
            cmd.setBackupId(p[1].trim());
            return cmd;
        }

        // LIST BACKUPS
        if (upper.equals("LIST BACKUPS")) {
            cmd.setType("LIST_BACKUPS");
            return cmd;
        }

        // CHECK INTEGRITY
        if (upper.equals("CHECK INTEGRITY")) {
            cmd.setType("CHECK_INTEGRITY");
            return cmd;
        }

        // PRUNE BACKUPS 7
        if (upper.startsWith("PRUNE BACKUPS")) {
            cmd.setType("PRUNE_BACKUPS");
            String[] p = input.trim().split("\\s+");
            cmd.setBackupId(p.length > 2 ? p[2].trim() : "7");
            return cmd;
        }

        // 5. Default parsing
        if (upper.startsWith("CREATE TABLE")) parseCreate(input, cmd);
        else if (upper.startsWith("INSERT INTO")) parseInsert(input, cmd);
        else if (upper.startsWith("SELECT")) parseSelect(input, cmd);
        else if (upper.startsWith("UPDATE")) parseUpdate(input, cmd);
        else if (upper.startsWith("DELETE FROM")) parseDelete(input, cmd);
        else if (upper.startsWith("DROP TABLE")) parseDrop(input, cmd);
        else cmd.setType("UNKNOWN");
        return cmd;
    }

    private static Command errorCmd(String msg) {
        Command cmd = new Command();
        cmd.setType("ERROR");
        cmd.setErrorMessage(msg);
        return cmd;
    }

    // CREATE INDEX ON products (id) PRIMARY
    private static void parseCreateIndex(String input, Command cmd) {
        cmd.setType("CREATE_INDEX");
        String upper = input.toUpperCase();
        int onIdx = upper.indexOf(" ON ");
        String indexType = "PRIMARY";
        if (onIdx > -1) {
            String beforeOn = input.substring(0, onIdx).trim();
            if (beforeOn.toUpperCase().contains("SECONDARY")) {
                indexType = "SECONDARY";
            } else if (beforeOn.toUpperCase().contains("PRIMARY")) {
                indexType = "PRIMARY";
            }
            
            String afterOn = input.substring(onIdx + 4).trim();
            int parenOpen = afterOn.indexOf("(");
            int parenClose = afterOn.indexOf(")");
            if (parenOpen > -1 && parenClose > parenOpen) {
                String tableName = afterOn.substring(0, parenOpen).trim();
                String columnName = afterOn.substring(parenOpen + 1, parenClose).trim();
                cmd.setTableName(tableName);
                cmd.setColumns(Collections.singletonList(columnName));
                
                String rest = afterOn.substring(parenClose + 1).trim().toUpperCase();
                if (rest.contains("SECONDARY")) {
                    indexType = "SECONDARY";
                } else if (rest.contains("PRIMARY")) {
                    indexType = "PRIMARY";
                }
            }
        }
        cmd.setIndexType(indexType);
    }

    // CREATE TABLE products (id,name,price,qty)
    private static void parseCreate(String input, Command cmd) {
        cmd.setType("CREATE");
        String[] parts = input.split("\\(");
        String[] words = parts[0].trim().split("\\s+");
        cmd.setTableName(words[2].trim());
        String colPart = parts[1].replace(")", "");
        String[] cols = colPart.split(",");
        List<String> columns = new ArrayList<>();
        for (String col : cols) columns.add(col.trim());
        cmd.setColumns(columns);
    }

    // INSERT INTO products VALUES (1,Rice,50,100)
    private static void parseInsert(String input, Command cmd) {
        cmd.setType("INSERT");
        String[] parts = input.split("(?i)VALUES");
        String[] words = parts[0].trim().split("\\s+");
        cmd.setTableName(words[2].trim());
        String valPart = parts[1]
            .replace("(","").replace(")","").trim();
        String[] vals = valPart.split(",");
        List<String> values = new ArrayList<>();
        for (String v : vals) values.add(v.trim());
        cmd.setValues(values);
    }

    // SELECT * FROM products WHERE price=50
    private static void parseSelect(String input, Command cmd) {
        cmd.setType("SELECT");
        String upper = input.toUpperCase();
        
        int fromIdx = upper.indexOf(" FROM ");
        int whereIdx = upper.indexOf(" WHERE ");
        int orderIdx = upper.indexOf(" ORDER BY ");
        int limitIdx = upper.indexOf(" LIMIT ");
        int joinIdx = upper.indexOf(" JOIN ");
        
        // Extract table name
        int tableEnd = (whereIdx > -1) ? whereIdx :
                       (orderIdx > -1) ? orderIdx :
                       (limitIdx > -1) ? limitIdx :
                       (joinIdx > -1) ? joinIdx :
                       input.length();
        
        cmd.setTableName(input.substring(fromIdx + 6, tableEnd).trim());
        
        // Extract columns (SELECT part)
        String colPart = input.substring(7, fromIdx).trim();
        if (!colPart.equals("*")) {
            String[] cols = colPart.split(",");
            List<String> columns = new ArrayList<>();
            for (String c : cols) {
                columns.add(c.trim());
            }
            cmd.setColumns(columns);
        }
        
        // Handle JOIN
        if (joinIdx > -1) {
            int joinEnd = (whereIdx > -1) ? whereIdx :
                           (orderIdx > -1) ? orderIdx :
                           (limitIdx > -1) ? limitIdx :
                           input.length();
            String joinClause = input.substring(joinIdx + 6, joinEnd).trim();
            int onIdx = joinClause.toUpperCase().indexOf(" ON ");
            if (onIdx > -1) {
                String joinTable = joinClause.substring(0, onIdx).trim();
                String joinOn = joinClause.substring(onIdx + 4).trim();
                cmd.setJoinTable(joinTable);
                cmd.setJoinOn(joinOn);
            }
        }
        
        // WHERE condition
        if (whereIdx > -1) {
            int condEnd = (orderIdx > -1) ? orderIdx :
                           (limitIdx > -1) ? limitIdx :
                           input.length();
            cmd.setCondition(input.substring(whereIdx + 7, condEnd).trim());
        }
        
        // ORDER BY
        if (orderIdx > -1) {
            int orderEnd = (limitIdx > -1) ? limitIdx : input.length();
            cmd.setOrderBy(input.substring(orderIdx + 10, orderEnd).trim());
        }
        
        // LIMIT
        if (limitIdx > -1) {
            cmd.setLimit(Integer.parseInt(input.substring(limitIdx + 7).trim()));
        }
    }

    // UPDATE products SET price=60 WHERE id=1
    private static void parseUpdate(String input, Command cmd) {
        cmd.setType("UPDATE");
        String upper = input.toUpperCase();
        int setIdx = upper.indexOf(" SET ");
        int whereIdx = upper.indexOf(" WHERE ");
        String[] words = input.substring(0,setIdx).trim().split("\\s+");
        cmd.setTableName(words[1].trim());
        String setClause = (whereIdx > -1)
            ? input.substring(setIdx+5, whereIdx).trim()
            : input.substring(setIdx+5).trim();
        cmd.setSetClause(setClause);
        if (whereIdx > -1)
            cmd.setCondition(input.substring(whereIdx+7).trim());
    }

    // DELETE FROM products WHERE id=1
    private static void parseDelete(String input, Command cmd) {
        cmd.setType("DELETE");
        String upper = input.toUpperCase();
        int whereIdx = upper.indexOf("WHERE");
        String tablePart = (whereIdx > -1)
            ? input.substring(11, whereIdx).trim()
            : input.substring(11).trim();
        cmd.setTableName(tablePart.trim());
        if (whereIdx > -1)
            cmd.setCondition(input.substring(whereIdx+5).trim());
    }

    // DROP TABLE products
    private static void parseDrop(String input, Command cmd) {
        cmd.setType("DROP");
        String[] words = input.trim().split("\\s+");
        cmd.setTableName(words[2].trim());
    }
}
