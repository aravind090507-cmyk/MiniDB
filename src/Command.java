import java.util.*;

public class Command {
    private String type;
    private String tableName;
    private List<String> columns;
    private List<String> values;
    private String condition;
    private String setClause;
    private String orderBy;
    private int limit = -1;
    private String joinTable;
    private String joinOn;

    // Model 6 Index/Search fields
    private String indexType;
    private String searchKeyword;

    // Model 7 Transaction fields
    private String transactionId; // e.g. "TXN-1"
    private String pendingSql; // SQL to queue in txn

    // Model 8 Authentication fields
    private String username; // for LOGIN / CREATE USER
    private String password; // for LOGIN / CREATE USER
    private String role; // for CREATE USER / CHANGE ROLE
    private String errorMessage; // for ERROR type commands

    // Model 9 Reporting fields
    private String reportType; // e.g. DAILY_SALES
    private String reportParam; // e.g. "2024-01-15" or "10"
    private boolean export; // true if EXPORT keyword present

    // Model 10 Backup fields
    private String backupId; // e.g. "backup_20240115_090000"

    public String getType() { return type; }
    public String getTableName() { return tableName; }
    public List<String> getColumns() { return columns; }
    public List<String> getValues() { return values; }
    public String getCondition() { return condition; }
    public String getSetClause() { return setClause; }
    public String getOrderBy() { return orderBy; }
    public int getLimit() { return limit; }
    public String getJoinTable() { return joinTable; }
    public String getJoinOn() { return joinOn; }

    // Model 6 Getters/Setters
    public String getIndexType() { return indexType; }
    public void setIndexType(String t) { indexType = t; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String s) { searchKeyword = s; }

    // Model 7 Getters/Setters
    public String getTransactionId() { return transactionId; }
    public String getPendingSql() { return pendingSql; }
    public void setTransactionId(String t) { transactionId = t; }
    public void setPendingSql(String s) { pendingSql = s; }

    // Model 8 Getters/Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getErrorMessage() { return errorMessage; }

    // Model 9 Getters/Setters
    public String getReportType() { return reportType; }
    public String getReportParam() { return reportParam; }
    public boolean isExport() { return export; }

    // Model 10 Getters/Setters
    public String getBackupId() { return backupId; }

    public void setType(String t) { type = t; }
    public void setTableName(String n) { tableName = n; }
    public void setColumns(List<String> c) { columns = c; }
    public void setValues(List<String> v) { values = v; }
    public void setCondition(String c) { condition = c; }
    public void setSetClause(String s) { setClause = s; }
    public void setOrderBy(String o) { orderBy = o; }
    public void setLimit(int l) { limit = l; }
    public void setJoinTable(String jt) { joinTable = jt; }
    public void setJoinOn(String jo) { joinOn = jo; }

    public void setUsername(String u) { username = u; }
    public void setPassword(String p) { password = p; }
    public void setRole(String r) { role = r; }
    public void setErrorMessage(String e) { errorMessage = e; }

    public void setReportType(String t) { reportType = t; }
    public void setReportParam(String p) { reportParam = p; }
    public void setExport(boolean e) { export = e; }
    public void setBackupId(String b) { backupId = b; }
}
