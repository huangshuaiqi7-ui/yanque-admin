package cn.yanque.modules.aitexttosql.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.aitexttosql.biz.TextToSqlQueryBiz;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlExecuteReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExecutionResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Text-to-SQL 内部查询中心接口。
 */
@RestController
@RequestMapping("/internal/ai/text-to-sql")
public class InternalTextToSqlController {
    private final TextToSqlQueryBiz queryBiz;

    public InternalTextToSqlController(TextToSqlQueryBiz queryBiz) {
        this.queryBiz = queryBiz;
    }

    /**
     * 执行模型生成的 SQL。
     *
     * 查询中心会在真正查库前先做 AST 校验；校验失败时直接返回失败结果。
     */
    @PostMapping("/sql/execute")
    public ApiResponse<SqlExecutionResult> executeSql(@Valid @RequestBody TextToSqlExecuteReq req) {
        return ApiResponse.success(queryBiz.executeSql(req));
    }
}
