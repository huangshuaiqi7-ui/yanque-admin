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
import net.sf.jsqlparser.statement.select.SubSelect;
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

            SqlValidationResult derivedColumnResult = validateDerivedColumns(select, usedTables);
            if (!derivedColumnResult.isValid()) {
                return derivedColumnResult;
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
     * 校验派生表字段。
     *
     * 例如 FROM (...) t 之后，外层可以使用 t.stat_date，
     * 但 stat_date 必须是子查询 SELECT 出来的字段或别名。
     */
    private SqlValidationResult validateDerivedColumns(Select select, List<String> usedTables) {
        List<String> unknownColumns = getUnknownDerivedColumns(select.getSelectBody());
        if (unknownColumns.isEmpty()) {
            return SqlValidationResult.success("", usedTables, Map.of());
        }
        SqlValidationResult result = SqlValidationResult.fail(
                "SQL使用了派生表未输出的字段：" + String.join("、", unknownColumns) + "。"
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
            collectSubSelectColumnMap(plainSelect, columnMap);
            SelectScope scope = buildSelectScope(plainSelect);
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                if (selectItem instanceof SelectExpressionItem expressionItem) {
                    collectColumnMap(expressionItem.getExpression(), columnMap, scope);
                }
            }
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                mergeColumnMap(columnMap, getSelectedColumnMap(child));
            }
        }
        return columnMap;
    }

    /**
     * 遍历 SELECT 语句，收集所有出现过的字段。
     *
     * 包含 SELECT、WHERE、JOIN ON、GROUP BY、ORDER BY、HAVING 以及子查询里的字段。
     */
    private void collectColumnMap(SelectBody selectBody, Map<String, List<String>> columnMap) {
        if (selectBody instanceof PlainSelect plainSelect) {
            collectSubSelectColumnMap(plainSelect, columnMap);
            SelectScope scope = buildSelectScope(plainSelect);
            ExpressionDeParser expressionVisitor = buildColumnMapVisitor(columnMap, scope);
            StringBuilder buffer = new StringBuilder();
            SelectDeParser selectVisitor = new SelectDeParser(expressionVisitor, buffer);
            expressionVisitor.setSelectVisitor(selectVisitor);
            expressionVisitor.setBuffer(buffer);
            selectBody.accept(selectVisitor);
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                collectColumnMap(child, columnMap);
            }
        }
    }

    /**
     * 遍历单个表达式里的字段。
     */
    private void collectColumnMap(
            Expression expression,
            Map<String, List<String>> columnMap,
            SelectScope scope
    ) {
        expression.accept(buildColumnMapVisitor(columnMap, scope));
    }

    /**
     * 构造字段收集 visitor。
     *
     * JSQLParser 在访问 Column 节点时会回调 visit(Column)，这里把字段归到真实表名下。
     */
    private ExpressionDeParser buildColumnMapVisitor(
            Map<String, List<String>> columnMap,
            SelectScope scope
    ) {
        return new ExpressionDeParser() {
            @Override
            public void visit(Column column) {
                String columnName = cleanSqlName(column.getColumnName());
                String tableName = resolveTableName(column, scope);
                if (StringUtils.hasText(tableName) && StringUtils.hasText(columnName) && !"*".equals(columnName)) {
                    addColumnToMap(columnMap, tableName, columnName);
                }
                super.visit(column);
            }
        };
    }

    /**
     * 先递归收集 FROM/JOIN 子查询里的字段。
     */
    private void collectSubSelectColumnMap(PlainSelect plainSelect, Map<String, List<String>> columnMap) {
        collectSubSelectColumnMap(plainSelect.getFromItem(), columnMap);
        if (plainSelect.getJoins() == null) {
            return;
        }
        for (Join join : plainSelect.getJoins()) {
            collectSubSelectColumnMap(join.getRightItem(), columnMap);
        }
    }

    /**
     * 如果 FROM/JOIN 目标是子查询，就递归提取子查询字段。
     */
    private void collectSubSelectColumnMap(FromItem fromItem, Map<String, List<String>> columnMap) {
        if (fromItem instanceof SubSelect subSelect) {
            collectColumnMap(subSelect.getSelectBody(), columnMap);
        }
    }

    /**
     * 找出派生表里不存在的字段。
     */
    private List<String> getUnknownDerivedColumns(SelectBody selectBody) {
        List<String> result = new ArrayList<>();
        collectUnknownDerivedColumns(selectBody, result);
        return unique(result);
    }

    /**
     * 递归遍历 SELECT，收集非法派生表字段。
     */
    private void collectUnknownDerivedColumns(SelectBody selectBody, List<String> result) {
        if (selectBody instanceof PlainSelect plainSelect) {
            collectSubSelectUnknownDerivedColumns(plainSelect, result);
            SelectScope scope = buildSelectScope(plainSelect);
            collectUnknownDerivedColumnsInCurrentSelect(plainSelect, scope, result);
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                collectUnknownDerivedColumns(child, result);
            }
        }
    }

    /**
     * 递归检查 FROM/JOIN 子查询内部的派生表字段。
     */
    private void collectSubSelectUnknownDerivedColumns(PlainSelect plainSelect, List<String> result) {
        collectSubSelectUnknownDerivedColumns(plainSelect.getFromItem(), result);
        if (plainSelect.getJoins() == null) {
            return;
        }
        for (Join join : plainSelect.getJoins()) {
            collectSubSelectUnknownDerivedColumns(join.getRightItem(), result);
        }
    }

    /**
     * 如果 FROM/JOIN 目标是子查询，就继续检查子查询内部。
     */
    private void collectSubSelectUnknownDerivedColumns(FromItem fromItem, List<String> result) {
        if (fromItem instanceof SubSelect subSelect) {
            collectUnknownDerivedColumns(subSelect.getSelectBody(), result);
        }
    }

    /**
     * 检查当前 SELECT 层级里使用的派生表字段。
     *
     * 这里只看当前层，子查询由外层递归方法负责。
     */
    private void collectUnknownDerivedColumnsInCurrentSelect(
            PlainSelect plainSelect,
            SelectScope scope,
            List<String> result
    ) {
        ExpressionDeParser visitor = buildUnknownDerivedColumnVisitor(scope, result);
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof SelectExpressionItem expressionItem) {
                visitExpression(expressionItem.getExpression(), visitor);
            }
        }
        visitExpression(plainSelect.getWhere(), visitor);
        visitExpression(plainSelect.getHaving(), visitor);
        if (plainSelect.getGroupBy() != null && plainSelect.getGroupBy().getGroupByExpressions() != null) {
            for (Expression expression : plainSelect.getGroupBy().getGroupByExpressions()) {
                visitExpression(expression, visitor);
            }
        }
        if (plainSelect.getOrderByElements() != null) {
            for (var orderByElement : plainSelect.getOrderByElements()) {
                visitExpression(orderByElement.getExpression(), visitor);
            }
        }
        if (plainSelect.getJoins() == null) {
            return;
        }
        for (Join join : plainSelect.getJoins()) {
            if (join.getOnExpressions() != null) {
                for (Expression expression : join.getOnExpressions()) {
                    visitExpression(expression, visitor);
                }
            }
        }
    }

    /**
     * 构造派生表字段检查 visitor。
     */
    private ExpressionDeParser buildUnknownDerivedColumnVisitor(SelectScope scope, List<String> result) {
        return new ExpressionDeParser() {
            @Override
            public void visit(Column column) {
                if (column.getTable() == null) {
                    super.visit(column);
                    return;
                }
                String tableName = cleanSqlName(column.getTable().getName());
                String columnName = cleanSqlName(column.getColumnName());
                if (scope.isDerivedTable(tableName) && !scope.isDerivedColumn(tableName, columnName)) {
                    result.add(columnKey(tableName, columnName));
                }
                super.visit(column);
            }
        };
    }

    /**
     * 访问一个可能为空的表达式。
     */
    private void visitExpression(Expression expression, ExpressionDeParser visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    /**
     * 找出没有写表名或表别名的字段。
     */
    private List<String> getUnqualifiedColumns(SelectBody selectBody) {
        List<String> columns = new ArrayList<>();
        collectUnqualifiedColumns(selectBody, columns, getSelectAliases(selectBody));
        return unique(columns);
    }

    /**
     * 递归收集裸字段。
     */
    private void collectUnqualifiedColumns(SelectBody selectBody, List<String> columns, Set<String> selectAliases) {
        if (selectBody instanceof PlainSelect plainSelect) {
            collectSubSelectUnqualifiedColumns(plainSelect, columns);
            ExpressionDeParser expressionVisitor = buildUnqualifiedColumnVisitor(columns, selectAliases);
            StringBuilder buffer = new StringBuilder();
            SelectDeParser selectVisitor = new SelectDeParser(expressionVisitor, buffer);
            expressionVisitor.setSelectVisitor(selectVisitor);
            expressionVisitor.setBuffer(buffer);
            selectBody.accept(selectVisitor);
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                collectUnqualifiedColumns(child, columns, getSelectAliases(child));
            }
        }
    }

    /**
     * 构造裸字段检查 visitor。
     */
    private ExpressionDeParser buildUnqualifiedColumnVisitor(List<String> columns, Set<String> selectAliases) {
        return new ExpressionDeParser() {
            @Override
            public void visit(Column column) {
                String columnName = cleanSqlName(column.getColumnName());
                boolean hasTableName = column.getTable() != null
                        && StringUtils.hasText(cleanSqlName(column.getTable().getName()));
                if (StringUtils.hasText(columnName)
                        && !"*".equals(columnName)
                        && !hasTableName
                        && !selectAliases.contains(columnName)) {
                    columns.add(columnName);
                }
                super.visit(column);
            }
        };
    }

    /**
     * 递归检查 FROM/JOIN 子查询里的裸字段。
     */
    private void collectSubSelectUnqualifiedColumns(PlainSelect plainSelect, List<String> columns) {
        collectSubSelectUnqualifiedColumns(plainSelect.getFromItem(), columns);
        if (plainSelect.getJoins() == null) {
            return;
        }
        for (Join join : plainSelect.getJoins()) {
            collectSubSelectUnqualifiedColumns(join.getRightItem(), columns);
        }
    }

    /**
     * 如果 FROM/JOIN 目标是子查询，就继续检查子查询内部。
     */
    private void collectSubSelectUnqualifiedColumns(FromItem fromItem, List<String> columns) {
        if (fromItem instanceof SubSelect subSelect) {
            collectUnqualifiedColumns(subSelect.getSelectBody(), columns, getSelectAliases(subSelect.getSelectBody()));
        }
    }

    /**
     * SELECT 返回列别名可以出现在 ORDER BY 里，例如：
     * select date(op.pay_success_time) as stat_date ... order by stat_date
     *
     * stat_date 不是表字段，不应该按“裸字段缺少表别名”拦截。
     */
    private Set<String> getSelectAliases(SelectBody selectBody) {
        Set<String> aliases = new LinkedHashSet<>();
        if (selectBody instanceof PlainSelect plainSelect) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                if (selectItem instanceof SelectExpressionItem expressionItem && expressionItem.getAlias() != null) {
                    aliases.add(cleanSqlName(expressionItem.getAlias().getName()));
                }
            }
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                aliases.addAll(getSelectAliases(child));
            }
        }
        return aliases;
    }

    /**
     * 收集真实表和表别名。
     *
     * 例如 order_payment op 会记录 order_payment -> order_payment、op -> order_payment。
     */
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

    /**
     * 构建当前 SELECT 的作用域。
     *
     * 作用域用于解析字段属于真实表、表别名，还是派生表。
     */
    private SelectScope buildSelectScope(PlainSelect plainSelect) {
        Map<String, String> tableAliases = new LinkedHashMap<>();
        Map<String, Set<String>> derivedTableColumns = new LinkedHashMap<>();
        addFromItemToScope(tableAliases, derivedTableColumns, plainSelect.getFromItem());
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                addFromItemToScope(tableAliases, derivedTableColumns, join.getRightItem());
            }
        }
        return new SelectScope(tableAliases, derivedTableColumns);
    }

    /**
     * 把一个 FROM/JOIN 项加入当前作用域。
     */
    private void addFromItemToScope(
            Map<String, String> tableAliases,
            Map<String, Set<String>> derivedTableColumns,
            FromItem fromItem
    ) {
        addTableAlias(tableAliases, fromItem);
        addDerivedTableAlias(derivedTableColumns, fromItem);
    }

    /**
     * 收集派生表别名和它输出的字段。
     *
     * 例如 FROM (select date(...) as stat_date) t 会记录 t -> [stat_date]。
     */
    private void addDerivedTableAlias(Map<String, Set<String>> derivedTableColumns, FromItem fromItem) {
        if (!(fromItem instanceof SubSelect subSelect) || subSelect.getAlias() == null) {
            return;
        }
        String alias = cleanSqlName(subSelect.getAlias().getName());
        if (!StringUtils.hasText(alias)) {
            return;
        }
        derivedTableColumns.put(alias, getSelectOutputNames(subSelect.getSelectBody()));
    }

    /**
     * 读取 SELECT 输出列名。
     *
     * 有别名用别名；没有别名且是普通字段时用字段名。
     */
    private Set<String> getSelectOutputNames(SelectBody selectBody) {
        Set<String> outputNames = new LinkedHashSet<>();
        if (selectBody instanceof PlainSelect plainSelect) {
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                if (!(selectItem instanceof SelectExpressionItem expressionItem)) {
                    continue;
                }
                if (expressionItem.getAlias() != null) {
                    outputNames.add(cleanSqlName(expressionItem.getAlias().getName()));
                } else if (expressionItem.getExpression() instanceof Column column) {
                    outputNames.add(cleanSqlName(column.getColumnName()));
                }
            }
        } else if (selectBody instanceof SetOperationList setOperationList) {
            for (SelectBody child : setOperationList.getSelects()) {
                outputNames.addAll(getSelectOutputNames(child));
            }
        }
        return outputNames;
    }

    /**
     * 把字段上的表名或别名解析成真实表名。
     *
     * 派生表字段不属于真实物理表，返回空字符串，让后续字段白名单校验跳过。
     */
    private String resolveTableName(Column column, SelectScope scope) {
        if (column.getTable() == null) {
            return "";
        }
        String tableName = cleanSqlName(column.getTable().getName());
        if (!StringUtils.hasText(tableName)) {
            return "";
        }
        if (scope.isDerivedColumn(tableName, cleanSqlName(column.getColumnName()))) {
            return "";
        }
        String realTableName = scope.tableAliases().get(tableName);
        return StringUtils.hasText(realTableName) ? realTableName : "";
    }

    /**
     * 把字段加入 table -> columns 映射，并保持去重。
     */
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

    /**
     * 合并两个 table -> columns 映射。
     */
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

    /**
     * 空值转空字符串并去掉前后空格。
     */
    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 清理 SQL 标识符。
     *
     * 目前主要去掉反引号，方便和 DDL Resource 里的表字段名比较。
     */
    private String cleanSqlName(String value) {
        return trim(value).replace("`", "");
    }

    /**
     * 生成 table.column 形式的字段 key。
     */
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
     * 一个 SELECT 的当前作用域。
     *
     * tableAliases：真实表别名，例如 op -> order_payment。
     * derivedTableColumns：派生表输出列，例如 t -> [stat_date, daily_order_count]。
     */
    private record SelectScope(Map<String, String> tableAliases, Map<String, Set<String>> derivedTableColumns) {
        /**
         * 判断某个别名是否是派生表。
         */
        boolean isDerivedTable(String tableAlias) {
            return derivedTableColumns.containsKey(tableAlias);
        }

        /**
         * 判断某个字段是否是派生表输出列。
         */
        boolean isDerivedColumn(String tableAlias, String columnName) {
            Set<String> columns = derivedTableColumns.get(tableAlias);
            return columns != null && columns.contains(columnName);
        }
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
