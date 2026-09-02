package cn.yanque.modules.aitexttosql.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Text-to-SQL SQL 执行器。
 */
@Service
public class TextToSqlSqlExecutor {
    private static final int QUERY_TIMEOUT_SECONDS = 10;

    private final JdbcTemplate jdbcTemplate;

    public TextToSqlSqlExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 使用只读 SELECT SQL 查询数据。
     *
     * SQL 走到这里时已经通过 AST 校验、权限校验和执行计划检查。
     */
    public List<Map<String, Object>> query(String sql, int maxRows) {
        return jdbcTemplate.query(
                connection -> {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setMaxRows(maxRows);
                    statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                    return statement;
                },
                (resultSet, rowNum) -> {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    Map<String, Object> row = new LinkedHashMap<>();
                    // 使用 columnLabel，保留 SQL 里的别名，前端展示会更友好。
                    for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                        row.put(metaData.getColumnLabel(columnIndex), resultSet.getObject(columnIndex));
                    }
                    return row;
                }
        );
    }
}
