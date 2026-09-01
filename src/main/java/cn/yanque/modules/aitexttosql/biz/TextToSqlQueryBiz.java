package cn.yanque.modules.aitexttosql.biz;

import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlExecuteReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExecutionResult;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExplainResult;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlPermissionCheckResult;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlValidationResult;
import cn.yanque.modules.aitexttosql.service.TextToSqlExplainChecker;
import cn.yanque.modules.aitexttosql.service.TextToSqlPermissionChecker;
import cn.yanque.modules.aitexttosql.service.TextToSqlSqlExecutor;
import cn.yanque.modules.aitexttosql.service.TextToSqlSqlValidator;
import org.springframework.stereotype.Component;

/**
 * Text-to-SQL 查询业务编排。
 */
@Component
public class TextToSqlQueryBiz {
    private static final int SYSTEM_MAX_ROWS = 500;

    private final TextToSqlSqlValidator sqlValidator;
    private final TextToSqlPermissionChecker permissionChecker;
    private final TextToSqlExplainChecker explainChecker;
    private final TextToSqlSqlExecutor sqlExecutor;

    public TextToSqlQueryBiz(
            TextToSqlSqlValidator sqlValidator,
            TextToSqlPermissionChecker permissionChecker,
            TextToSqlExplainChecker explainChecker,
            TextToSqlSqlExecutor sqlExecutor
    ) {
        this.sqlValidator = sqlValidator;
        this.permissionChecker = permissionChecker;
        this.explainChecker = explainChecker;
        this.sqlExecutor = sqlExecutor;
    }

    /**
     * 执行模型生成的 SQL。
     *
     * 真正查库前必须先过 AST 校验；没过校验时不会碰数据库。
     */
    public SqlExecutionResult executeSql(TextToSqlExecuteReq req) {

        // 使用的表 和 字段在数据库都存在
        SqlValidationResult validation = sqlValidator.validate(req.getSql(), req.getTableDdlContext());
        if (!validation.isValid()) {
            return SqlExecutionResult.validationFailed(validation);
        }

        // 用户是否有sql的表和字段权限
        TextToSqlPermissionCheckResult permission = permissionChecker.check(req, validation);
        if (!permission.isAllowed()) {
            return SqlExecutionResult.permissionDenied(permission);
        }

        // 先拿执行计划，记录访问类型、索引、预估扫描行数、临时表和排序等信息。
        // 第一版只强制要求实际使用索引；临时表、排序和预估扫描行数先只返回展示。
        SqlExplainResult explain = explainChecker.check(validation.getNormalizedSql());
        if (!explain.isAllowed()) {
            return SqlExecutionResult.explainDenied(
                    validation.getNormalizedSql(),
                    validation,
                    permission,
                    explain
            );
        }

        int maxRows = normalizeMaxRows(req.getMaxRows());
        return SqlExecutionResult.success(
                validation.getNormalizedSql(),
                validation,
                permission,
                explain,
                sqlExecutor.query(validation.getNormalizedSql(), maxRows)
        );
    }

    private int normalizeMaxRows(Integer requestMaxRows) {
        int requestLimit = (requestMaxRows == null || requestMaxRows <= 0) ? 100 : requestMaxRows;
        return Math.min(requestLimit, SYSTEM_MAX_ROWS);
    }
}
