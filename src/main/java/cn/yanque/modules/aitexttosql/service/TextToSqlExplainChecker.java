package cn.yanque.modules.aitexttosql.service;

import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExplainPlanRow;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExplainResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Text-to-SQL 执行计划检查。
 *
 * 当前只用 EXPLAIN 把访问类型、索引、预估扫描行数、临时表和排序信息取出来。
 * 第一版只拦截没有实际使用索引的 SQL。
 */
@Service
public class TextToSqlExplainChecker {
    private static final int EXPLAIN_TIMEOUT_SECONDS = 5;

    private final JdbcTemplate jdbcTemplate;

    public TextToSqlExplainChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 对 SQL 执行 EXPLAIN，并返回是否允许继续查询。
     *
     * EXPLAIN 自身失败时不直接抛错，而是返回 skipped，避免执行计划检查影响正常查询调试。
     */
    public SqlExplainResult check(String sql) {
        try {
            List<SqlExplainPlanRow> plans = jdbcTemplate.query(
                    "EXPLAIN " + sql,
                        (resultSet, rowNum) -> toPlanRow(resultSet)


            );
            return evaluatePlans(plans);
        } catch (Exception ex) {
            return SqlExplainResult.skipped("SQL执行计划检查失败：" + ex.getMessage());
        }
    }

    /**
     * 根据执行计划判断是否放行。
     *
     * 当前规则很简单：真实业务表没有实际使用索引时拒绝执行。
     */
    SqlExplainResult evaluatePlans(List<SqlExplainPlanRow> plans) {
        SqlExplainPlanRow noIndexPlan = findNoIndexPlan(plans);
        if (noIndexPlan != null) {
            return SqlExplainResult.denied(
                    "SQL执行计划未使用索引，已拒绝执行。表：" + noIndexPlan.getTableName() + "。",
                    plans
            );
        }
        return SqlExplainResult.success(plans);
    }

    /**
     * 找到第一条没有使用索引的真实表访问计划。
     */
    private SqlExplainPlanRow findNoIndexPlan(List<SqlExplainPlanRow> plans) {
        for (SqlExplainPlanRow plan : plans) {
            if (shouldCheckIndex(plan) && !hasText(plan.getUsedKey())) {
                return plan;
            }
        }
        return null;
    }

    /**
     * 只对真实业务表要求使用索引。
     *
     * EXPLAIN 里 <derived2>、<derived3> 是 MySQL 物化出来的派生表，
     * 它们不是数据库真实表，本身也没有业务索引，不能因为 key 为空就拦截。
     */
    private boolean shouldCheckIndex(SqlExplainPlanRow plan) {
        String tableName = plan.getTableName();
        if (!hasText(tableName)) {
            return false;
        }
        return !tableName.startsWith("<derived")
                && !tableName.startsWith("<subquery")
                && !"<union>".equals(tableName);
    }

    /**
     * 把 MySQL EXPLAIN 的一行结果转成前端可展示的对象。
     */
    private SqlExplainPlanRow toPlanRow(ResultSet resultSet) throws SQLException {
        SqlExplainPlanRow row = new SqlExplainPlanRow();
        row.setId(text(resultSet, "id"));
        row.setSelectType(text(resultSet, "select_type"));
        row.setTableName(text(resultSet, "table"));
        row.setAccessType(text(resultSet, "type"));
        row.setPossibleKeys(text(resultSet, "possible_keys"));
        row.setUsedKey(text(resultSet, "key"));
        row.setKeyLength(text(resultSet, "key_len"));
        row.setRef(text(resultSet, "ref"));
        row.setRows(longValue(resultSet, "rows"));
        row.setFiltered(text(resultSet, "filtered"));
        row.setExtra(text(resultSet, "Extra"));

        String extra = row.getExtra() == null ? "" : row.getExtra().toLowerCase(Locale.ROOT);
        row.setUsingTemporary(extra.contains("using temporary"));
        row.setUsingFilesort(extra.contains("using filesort"));
        return row;
    }

    /**
     * 从 ResultSet 中安全读取文本列。
     */
    private String text(ResultSet resultSet, String columnName) throws SQLException {
        Object value = resultSet.getObject(columnName);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 从 ResultSet 中安全读取 long 值。
     */
    private Long longValue(ResultSet resultSet, String columnName) throws SQLException {
        Object value = resultSet.getObject(columnName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
