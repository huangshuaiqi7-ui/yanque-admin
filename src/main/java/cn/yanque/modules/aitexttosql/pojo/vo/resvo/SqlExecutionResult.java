package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Text-to-SQL 查询执行结果。
 */
@Data
public class SqlExecutionResult {
    private boolean success;
    private String message;
    private String sql;
    private SqlValidationResult validation;
    private TextToSqlPermissionCheckResult permission;
    private SqlExplainResult explain;
    private List<Map<String, Object>> rows = new ArrayList<>();
    private int rowCount;

    public static SqlExecutionResult validationFailed(SqlValidationResult validation) {
        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(false);
        result.setMessage(validation.getReason());
        result.setValidation(validation);
        return result;
    }

    public static SqlExecutionResult permissionDenied(TextToSqlPermissionCheckResult permission) {
        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(false);
        result.setMessage(permission.getReason());
        result.setPermission(permission);
        return result;
    }

    public static SqlExecutionResult explainDenied(
            String sql,
            SqlValidationResult validation,
            TextToSqlPermissionCheckResult permission,
            SqlExplainResult explain
    ) {
        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(false);
        result.setMessage(explain.getMessage());
        result.setSql(sql);
        result.setValidation(validation);
        result.setPermission(permission);
        result.setExplain(explain);
        return result;
    }

    public static SqlExecutionResult success(
            String sql,
            SqlValidationResult validation,
            TextToSqlPermissionCheckResult permission,
            SqlExplainResult explain,
            List<Map<String, Object>> rows
    ) {
        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(true);
        result.setMessage("SQL执行成功。");
        result.setSql(sql);
        result.setValidation(validation);
        result.setPermission(permission);
        result.setExplain(explain);
        result.setRows(rows);
        result.setRowCount(rows.size());
        return result;
    }
}
