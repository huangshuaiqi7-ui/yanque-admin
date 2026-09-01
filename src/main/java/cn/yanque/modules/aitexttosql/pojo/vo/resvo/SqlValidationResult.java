package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL 校验结果。
 */
@Data
public class SqlValidationResult {
    private boolean valid;
    private String reason;
    private String normalizedSql;
    private List<String> usedTables = new ArrayList<>();
    /**
     * SQL 实际使用的表字段映射。
     *
     * 示例：
     * {
     *   "order_payment": ["order_no", "status"]
     * }
     */
    private Map<String, List<String>> usedColumnMap = new LinkedHashMap<>();
    private List<String> deniedColumns = new ArrayList<>();
    private List<String> maskedColumns = new ArrayList<>();

    public static SqlValidationResult success(
            String normalizedSql,
            List<String> usedTables,
            Map<String, List<String>> usedColumnMap
    ) {
        SqlValidationResult result = new SqlValidationResult();
        result.setValid(true);
        result.setReason("SQL校验通过。");
        result.setNormalizedSql(normalizedSql);
        result.setUsedTables(usedTables);
        result.setUsedColumnMap(usedColumnMap);
        return result;
    }

    public static SqlValidationResult fail(String reason) {
        SqlValidationResult result = new SqlValidationResult();
        result.setValid(false);
        result.setReason(reason);
        return result;
    }
}
