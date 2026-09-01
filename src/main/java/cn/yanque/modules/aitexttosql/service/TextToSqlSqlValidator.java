package cn.yanque.modules.aitexttosql.service;

import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlValidationResult;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Text-to-SQL 查询执行前的 SQL 安全校验。
 *
 * 这层是 Java 查询中心的安全边界：
 * 1. 先用 JSQLParser 把 SQL 解析成 AST，不靠字符串猜 SQL 结构；
 * 2. 再确认它是一条 SELECT；
 * 3. 然后检查 SQL 里用到的表、字段都来自 DDL Resource；
 * 4. 最后检查字段策略，例如 DENY 字段不能出现，MASK 字段不能直接返回。
 */
@Service
public class TextToSqlSqlValidator {
    /**
     * 校验模型生成的 SQL。
     *
     * 返回 valid=false 时，调用方不能继续执行 SQL。
     */
    public SqlValidationResult validate(String sql, String tableDdlContext) {
        try {
            String normalizedSql = sql == null ? "" : sql.trim();
            if (!StringUtils.hasText(normalizedSql)) {
                return SqlValidationResult.fail("SQL不能为空。");
            }

            Statement statement = parseOneStatement(normalizedSql);
            if (!(statement instanceof Select select)) {
                return SqlValidationResult.fail("SQL必须是SELECT查询。");
            }

            // DDL Resource 是模型可用表字段的白名单，SQL 只能使用这里声明过的表和字段。
            DdlInfo ddlInfo = loadDdlInfo(tableDdlContext);
            // TablesNamesFinder 会遍历 AST 中的 FROM/JOIN 等位置，提取 SQL 实际访问的表。
            List<String> usedTables = unique(new TablesNamesFinder().getTableList(statement));
            SqlValidationResult tableResult = validateTables(usedTables, ddlInfo);
            if (!tableResult.isValid()) {
                return tableResult;
            }

            // SELECT * 会把整表字段暴露出去，敏感字段也可能被一起查出，所以第一版直接禁止。
            if (hasSelectStar(select.getSelectBody())) {
                SqlValidationResult result = SqlValidationResult.fail("SQL不能使用SELECT *或表.*。");
                result.setUsedTables(usedTables);
                return result;
            }

            SqlValidationResult qualifiedColumnResult = validateColumnQualifier(select, usedTables);
            if (!qualifiedColumnResult.isValid()) {
                return qualifiedColumnResult;
            }

            // 字段会出现在 SELECT、WHERE、GROUP BY、ORDER BY、JOIN ON、函数参数等地方。
            // 这里按表归类，例如 order_payment -> [order_no, status]。
            Map<String, List<String>> usedColumnMap = getUsedColumnMap(select);
            SqlValidationResult columnResult = validateColumns(usedTables, usedColumnMap, ddlInfo);
            if (!columnResult.isValid()) {
                return columnResult;
            }

            SqlValidationResult policyResult = validateColumnPolicies(select, usedTables, usedColumnMap, ddlInfo);
            if (!policyResult.isValid()) {
                return policyResult;
            }

            return SqlValidationResult.success(statement.toString(), usedTables, usedColumnMap);
        } catch (SqlValidateException exception) {
            return exception.result;
        }
    }

    /**
     * 用 JSQLParser 解析 SQL，并确认只解析出一条语句。
     *
     * 例如结尾带一个分号的 SELECT 会被解析成一条语句，可以通过；
     * SELECT ...; DELETE ... 这种会被解析成两条语句，直接失败。
     */
    private Statement parseOneStatement(String sql) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sql);
            if (statements.getStatements().size() != 1) {
                throw new SqlValidateException(SqlValidationResult.fail("只能生成一条SQL。"));
            }
            return statements.getStatements().get(0);
        } catch (JSQLParserException exception) {
            throw new SqlValidateException(SqlValidationResult.fail("SQL语法解析失败。"), exception);
        }
    }

    /**
     * 把 DDL Resource JSON 转成 Java 内部更好用的结构。
     *
     * 入参可以是多表聚合格式：
     * {"ddls":[{"table_name":"order_payment","columns":[{"name":"order_no","query_policy":"ALLOW"}]}]}
     *
     * 也可以是单个 Bear DDL Resource：
     * {"table_name":"order_payment","columns":[{"name":"order_no","query_policy":"ALLOW"}]}
     *
     * 返回后会变成：
     * tableName -> 当前表的字段策略列表。
     */
    private DdlInfo loadDdlInfo(String tableDdlContext) {
        try {
            JSONObject data = JSON.parseObject(tableDdlContext);
            JSONArray ddls = data.getJSONArray("ddls");
            Map<String, List<ColumnPolicy>> tableColumns = new LinkedHashMap<>();

            if (ddls != null) {
                for (int i = 0; i < ddls.size(); i++) {
                    addDdlTable(tableColumns, ddls.getJSONObject(i));
                }
                return new DdlInfo(tableColumns);
            }

            addDdlTable(tableColumns, data);
            return new DdlInfo(tableColumns);
        } catch (Exception exception) {
            throw new SqlValidateException(SqlValidationResult.fail("DDL上下文不是合法JSON，无法校验SQL。"), exception);
        }
    }

    /**
     * 从一个表 DDL JSON 中取出表名和字段策略，放入校验白名单。
     */
    private void addDdlTable(Map<String, List<ColumnPolicy>> tableColumns, JSONObject ddl) {
        if (ddl == null) {
            return;
        }
        String tableName = trim(ddl.getString("table_name"));
        if (!StringUtils.hasText(tableName)) {
            return;
        }
        tableColumns.put(tableName, readColumns(tableName, ddl.getJSONArray("columns")));
    }

    /**
     * 读取一张表的字段列表。
     *
     * query_policy 不传时按 ALLOW 处理，避免 DDL Resource 早期字段不完整时全部误杀。
     */
    private List<ColumnPolicy> readColumns(String tableName, JSONArray columns) {
        List<ColumnPolicy> result = new ArrayList<>();
        if (columns == null) {
            return result;
        }
        for (int i = 0; i < columns.size(); i++) {
            JSONObject column = columns.getJSONObject(i);
            String columnName = trim(column.getString("name"));
            if (!StringUtils.hasText(columnName)) {
                continue;
            }
            String queryPolicy = trim(column.getString("query_policy")).toUpperCase(Locale.ROOT);
            if (!StringUtils.hasText(queryPolicy)) {
                queryPolicy = "ALLOW";
            }
            result.add(new ColumnPolicy(tableName, columnName, queryPolicy));
        }
        return result;
    }

    /**
     * 校验 SQL 用到的表是否都在 DDL Resource 里。
     * usedTables遍历 查看table是否在ddlInfo中
     */
    private SqlValidationResult validateTables(List<String> usedTables, DdlInfo ddlInfo) {
        List<String> unknownTables = new ArrayList<>();
        for (String table : usedTables) {
            if (!ddlInfo.tableColumns().containsKey(table)) {
                unknownTables.add(table);
            }
        }
        if (unknownTables.isEmpty()) {
            return SqlValidationResult.success("", usedTables, Map.of());
        }
        SqlValidationResult result = SqlValidationResult.fail("SQL使用了未声明的表：" + String.join("、", unknownTables) + "。");
        result.setUsedTables(usedTables);
        return result;
    }

    /**
     * 校验 SQL 用到的字段是否都在 DDL Resource 里。
     *
     * 第一版先不做“字段必须属于某个表别名”的精细校验。
     * 只要字段存在于本次 DDL 白名单任意表中，就先认为字段存在。
     */
    private SqlValidationResult validateColumns(
            List<String> usedTables,
            Map<String, List<String>> usedColumnMap,
            DdlInfo ddlInfo
    ) {
        Map<String, Set<String>> knownColumnMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<ColumnPolicy>> entry : ddlInfo.tableColumns().entrySet()) {
            String tableName = cleanSqlName(entry.getKey());
            Set<String> columns = new LinkedHashSet<>();
            List<ColumnPolicy> columnPolicies = entry.getValue();
            for (ColumnPolicy columnPolicy : columnPolicies) {
                columns.add(cleanSqlName(columnPolicy.columnName()));
            }
            knownColumnMap.put(tableName, columns);
        }

        List<String> unknownColumns = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : usedColumnMap.entrySet()) {
            String tableName = cleanSqlName(entry.getKey());
            Set<String> knownColumns = knownColumnMap.get(tableName);
            for (String column : entry.getValue()) {
                if (knownColumns == null || !knownColumns.contains(cleanSqlName(column))) {
                    unknownColumns.add(columnKey(tableName, column));
                }
            }
        }
        if (unknownColumns.isEmpty()) {
            return SqlValidationResult.success("", usedTables, usedColumnMap);
        }

        SqlValidationResult result = SqlValidationResult.fail("SQL使用了未声明的字段：" + String.join("、", unknownColumns) + "。");
        result.setUsedTables(usedTables);
        result.setUsedColumnMap(usedColumnMap);
        return result;
    }

    /**
     * 校验字段查询策略。
     *
     * DENY：任何位置都不能使用，比如内部唯一号、密码、密钥等。
     * MASK：第一版只禁止直接放在 SELECT 返回列里；WHERE 里用来过滤暂时允许。
     */
    private SqlValidationResult validateColumnPolicies(
            Select select,
            List<String> usedTables,
            Map<String, List<String>> usedColumnMap,
            DdlInfo ddlInfo
    ) {
        List<ColumnPolicy> columnPolicies = flattenColumnPolicies(ddlInfo);
        List<String> deniedColumns = findColumnsByPolicy(usedColumnMap, columnPolicies, "DENY");
        if (!deniedColumns.isEmpty()) {
            SqlValidationResult result = SqlValidationResult.fail("SQL使用了禁用字段：" + String.join("、", deniedColumns) + "。");
            result.setUsedTables(usedTables);
            result.setUsedColumnMap(usedColumnMap);
            result.setDeniedColumns(deniedColumns);
            return result;
        }

        Map<String, List<String>> selectedColumnMap = getSelectedColumnMap(select.getSelectBody());
        List<String> maskedColumns = findColumnsByPolicy(selectedColumnMap, columnPolicies, "MASK");
        if (!maskedColumns.isEmpty()) {
            SqlValidationResult result = SqlValidationResult.fail("SQL不能直接返回需要掩码的字段：" + String.join("、", maskedColumns) + "。");
            result.setUsedTables(usedTables);
            result.setUsedColumnMap(usedColumnMap);
            result.setMaskedColumns(maskedColumns);
            return result;
        }

        return SqlValidationResult.success("", usedTables, usedColumnMap);
    }

    /**
     * 校验字段必须写表名或表别名。
     *
     * 允许：select op.order_no from order_payment op
     * 禁止：select order_no from order_payment op
     */
    private SqlValidationResult validateColumnQualifier(Select select, List<String> usedTables) {
        List<String> unqualifiedColumns = getUnqualifiedColumns(select.getSelectBody());
        if (unqualifiedColumns.isEmpty()) {
            return SqlValidationResult.success("", usedTables, Map.of());
        }

        SqlValidationResult result = SqlValidationResult.fail(
                "SQL字段必须带表名或表别名：" + String.join("、", unqualifiedColumns) + "。"
        );
        result.setUsedTables(usedTables);
        return result;
    }

    /**
     * 检查 SELECT 返回项里有没有 *。
     *
     * PlainSelect 表示普通 SELECT。
     * SetOperationList 表示 UNION / INTERSECT / EXCEPT 这一类组合查询，这里递归检查每个子 SELECT。
     */
    private boolean hasSelectStar(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect plainSelect) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                if (selectItem instanceof AllColumns || selectItem instanceof AllTableColumns) {
                    return true;
                }
            }
            return false;
        }
        if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                if (hasSelectStar(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 提取整条 SQL 里所有出现过的字段名。
     */
    private Map<String, List<String>> getUsedColumnMap(Select select) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        collectColumnMap(select.getSelectBody(), result);
        return result;
    }

    /**
     * 只提取 SELECT 返回列里出现的字段名。
     *
     * 这个方法专门给 MASK 策略用：
     * SELECT student_phone 会被拦；
     * WHERE student_phone = '...' 第一版暂时允许。
     */
    private Map<String, List<String>> getSelectedColumnMap(SelectBody selectBody) {
        Map<String, List<String>> columnMap = new LinkedHashMap<>();
        if (selectBody instanceof PlainSelect plainSelect) {
            Map<String, String> tableAliases = readTableAliases(selectBody);
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                if (selectItem instanceof SelectExpressionItem expressionItem) {
                    collectColumnMap(expressionItem.getExpression(), columnMap, tableAliases);
                }
            }
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                mergeColumnMap(columnMap, getSelectedColumnMap(child));
            }
        }
        return columnMap;
    }

    private void collectColumnMap(SelectBody selectBody, Map<String, List<String>> columnMap) {
        Map<String, String> tableAliases = readTableAliases(selectBody);
        ExpressionDeParser expressionVisitor = buildColumnMapVisitor(columnMap, tableAliases);
        StringBuilder buffer = new StringBuilder();
        SelectDeParser selectVisitor = new SelectDeParser(expressionVisitor, buffer);
        expressionVisitor.setSelectVisitor(selectVisitor);
        expressionVisitor.setBuffer(buffer);
        selectBody.accept(selectVisitor);
    }

    private void collectColumnMap(
            Expression expression,
            Map<String, List<String>> columnMap,
            Map<String, String> tableAliases
    ) {
        expression.accept(buildColumnMapVisitor(columnMap, tableAliases));
    }

    private ExpressionDeParser buildColumnMapVisitor(
            Map<String, List<String>> columnMap,
            Map<String, String> tableAliases
    ) {
        return new ExpressionDeParser() {
            @Override
            public void visit(Column column) {
                String columnName = cleanSqlName(column.getColumnName());
                String tableName = resolveTableName(column, tableAliases);
                if (StringUtils.hasText(tableName) && StringUtils.hasText(columnName) && !"*".equals(columnName)) {
                    addColumnToMap(columnMap, tableName, columnName);
                }
                super.visit(column);
            }
        };
    }

    private List<String> getUnqualifiedColumns(SelectBody selectBody) {
        List<String> columns = new ArrayList<>();
        collectUnqualifiedColumns(selectBody, columns);
        return unique(columns);
    }

    private void collectUnqualifiedColumns(SelectBody selectBody, List<String> columns) {
        ExpressionDeParser expressionVisitor = buildUnqualifiedColumnVisitor(columns);
        StringBuilder buffer = new StringBuilder();
        SelectDeParser selectVisitor = new SelectDeParser(expressionVisitor, buffer);
        expressionVisitor.setSelectVisitor(selectVisitor);
        expressionVisitor.setBuffer(buffer);
        selectBody.accept(selectVisitor);
    }

    private ExpressionDeParser buildUnqualifiedColumnVisitor(List<String> columns) {
        return new ExpressionDeParser() {
            @Override
            public void visit(Column column) {
                String columnName = cleanSqlName(column.getColumnName());
                boolean hasTableName = column.getTable() != null
                        && StringUtils.hasText(cleanSqlName(column.getTable().getName()));
                if (StringUtils.hasText(columnName) && !"*".equals(columnName) && !hasTableName) {
                    columns.add(columnName);
                }
                super.visit(column);
            }
        };
    }

    /**
     * 读取 FROM / JOIN 中的表别名。
     *
     * 例如 order_payment op 会记录：
     * - op -> order_payment
     * - order_payment -> order_payment
     */
    private Map<String, String> readTableAliases(SelectBody selectBody) {
        Map<String, String> result = new LinkedHashMap<>();
        if (selectBody instanceof PlainSelect plainSelect) {
            addTableAlias(result, plainSelect.getFromItem());
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    addTableAlias(result, join.getRightItem());
                }
            }
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                result.putAll(readTableAliases(child));
            }
        }
        return result;
    }

    private void addTableAlias(Map<String, String> tableAliases, FromItem fromItem) {
        if (!(fromItem instanceof Table table)) {
            return;
        }
        String tableName = cleanSqlName(table.getName());
        if (!StringUtils.hasText(tableName)) {
            return;
        }
        tableAliases.put(tableName, tableName);
        if (table.getAlias() != null) {
            tableAliases.put(cleanSqlName(table.getAlias().getName()), tableName);
        }
    }

    private String resolveTableName(Column column, Map<String, String> tableAliases) {
        if (column.getTable() == null) {
            return "";
        }
        String tableName = cleanSqlName(column.getTable().getName());
        if (!StringUtils.hasText(tableName)) {
            return "";
        }
        String realTableName = tableAliases.get(tableName);
        return StringUtils.hasText(realTableName) ? realTableName : tableName;
    }

    private void addColumnToMap(Map<String, List<String>> columnMap, String tableName, String columnName) {
        List<String> columns = columnMap.computeIfAbsent(tableName, ignored -> new ArrayList<>());
        if (!columns.contains(columnName)) {
            columns.add(columnName);
        }
    }

    /**
     * 把 table -> columns 的结构拍平成一个字段策略列表，方便后面查 DENY / MASK。
     */
    private List<ColumnPolicy> flattenColumnPolicies(DdlInfo ddlInfo) {
        List<ColumnPolicy> result = new ArrayList<>();
        for (List<ColumnPolicy> columnPolicies : ddlInfo.tableColumns().values()) {
            result.addAll(columnPolicies);
        }
        return result;
    }

    /**
     * 从字段名列表里找出命中某个策略的字段。
     */
    private List<String> findColumnsByPolicy(
            Map<String, List<String>> usedColumnMap,
            List<ColumnPolicy> columnPolicies,
            String policy
    ) {
        Set<String> policyColumns = new LinkedHashSet<>();
        for (ColumnPolicy columnPolicy : columnPolicies) {
            if (policy.equals(columnPolicy.queryPolicy())) {
                policyColumns.add(columnKey(columnPolicy.tableName(), columnPolicy.columnName()));
            }
        }

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : usedColumnMap.entrySet()) {
            for (String column : entry.getValue()) {
                String columnKey = columnKey(entry.getKey(), column);
                if (policyColumns.contains(columnKey)) {
                    result.add(columnKey);
                }
            }
        }
        return unique(result);
    }

    private void mergeColumnMap(Map<String, List<String>> target, Map<String, List<String>> source) {
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            for (String column : entry.getValue()) {
                addColumnToMap(target, entry.getKey(), column);
            }
        }
    }

    /**
     * 保持原始顺序去重。
     *
     * LinkedHashSet 能记住插入顺序，所以返回结果更方便排查 SQL 里字段出现的位置。
     */
    private List<String> unique(List<String> values) {
        List<String> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String value : values) {
            if (!StringUtils.hasText(value) || seen.contains(value)) {
                continue;
            }
            seen.add(value);
            result.add(value);
        }
        return result;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanSqlName(String value) {
        return trim(value).replace("`", "");
    }

    private String columnKey(String tableName, String columnName) {
        return cleanSqlName(tableName) + "." + cleanSqlName(columnName);
    }

    /**
     * DDL Resource 解析后的表字段白名单。
     */
    private record DdlInfo(Map<String, List<ColumnPolicy>> tableColumns) {
    }

    /**
     * 单个字段的查询策略。
     */
    private record ColumnPolicy(String tableName, String columnName, String queryPolicy) {
    }

    /**
     * 内部异常，只用于从深层 helper 方法提前返回校验失败结果。
     */
    private static class SqlValidateException extends RuntimeException {
        private final SqlValidationResult result;

        SqlValidateException(SqlValidationResult result) {
            this.result = result;
        }

        SqlValidateException(SqlValidationResult result, Throwable cause) {
            super(cause);
            this.result = result;
        }
    }
}
