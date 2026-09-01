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

    private SqlExplainPlanRow findNoIndexPlan(List<SqlExplainPlanRow> plans) {
        for (SqlExplainPlanRow plan : plans) {
            if (!hasText(plan.getUsedKey())) {
                return plan;
            }
        }
        return null;
    }

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

    private String text(ResultSet resultSet, String columnName) throws SQLException {
        Object value = resultSet.getObject(columnName);
        return value == null ? null : String.valueOf(value);
    }

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
