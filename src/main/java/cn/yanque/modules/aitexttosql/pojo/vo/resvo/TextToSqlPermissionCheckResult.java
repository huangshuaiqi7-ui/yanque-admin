package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Text-to-SQL 角色数据权限校验结果。
 */
@Data
public class TextToSqlPermissionCheckResult {
    private boolean allowed;
    private String reason;
    private List<String> deniedTables = new ArrayList<>();
    private List<String> deniedColumns = new ArrayList<>();

    public static TextToSqlPermissionCheckResult allowed() {
        TextToSqlPermissionCheckResult result = new TextToSqlPermissionCheckResult();
        result.setAllowed(true);
        result.setReason("数据权限校验通过。");
        return result;
    }

    public static TextToSqlPermissionCheckResult denied(String reason) {
        TextToSqlPermissionCheckResult result = new TextToSqlPermissionCheckResult();
        result.setAllowed(false);
        result.setReason(reason);
        return result;
    }
}
