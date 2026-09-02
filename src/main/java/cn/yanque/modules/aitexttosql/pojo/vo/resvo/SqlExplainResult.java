package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Text-to-SQL SQL 执行计划检查结果。
 *
 * 第一版只强制要求 SQL 实际使用索引；临时表、排序、预估扫描行数先只展示。
 */
@Data
public class SqlExplainResult {
    private boolean checked;
    private boolean allowed;
    private String message;
    private List<SqlExplainPlanRow> plans = new ArrayList<>();

    /**
     * 执行计划检查通过。
     */
    public static SqlExplainResult success(List<SqlExplainPlanRow> plans) {
        SqlExplainResult result = new SqlExplainResult();
        result.setChecked(true);
        result.setAllowed(true);
        result.setMessage("SQL执行计划检查完成。");
        result.setPlans(plans);
        return result;
    }

    /**
     * 执行计划检查拒绝执行。
     */
    public static SqlExplainResult denied(String message, List<SqlExplainPlanRow> plans) {
        SqlExplainResult result = new SqlExplainResult();
        result.setChecked(true);
        result.setAllowed(false);
        result.setMessage(message);
        result.setPlans(plans);
        return result;
    }

    /**
     * 执行计划检查跳过。
     *
     * 通常表示 EXPLAIN 自身失败，调用方可以看到原因。
     */
    public static SqlExplainResult skipped(String message) {
        SqlExplainResult result = new SqlExplainResult();
        result.setChecked(false);
        result.setAllowed(false);
        result.setMessage(message);
        return result;
    }
}
