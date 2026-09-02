package cn.yanque.modules.aitexttosql.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.context.UserContext;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalTaskEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalContinueReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalResultPageReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalTaskCreateReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalTaskPageReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlEvalResultRes;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlEvalTaskDetailRes;
import cn.yanque.modules.aitexttosql.service.TextToSqlEvalTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Text-to-SQL 评测任务接口。
 */
@RestController
@RequestMapping("/api/ai/text-to-sql/eval-tasks")
public class TextToSqlEvalTaskController {
    private final TextToSqlEvalTaskService service;

    public TextToSqlEvalTaskController(TextToSqlEvalTaskService service) {
        this.service = service;
    }

    /**
     * 创建评测任务，并立即后台执行。
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody TextToSqlEvalTaskCreateReq req) {
        return ApiResponse.success(Map.of("id", service.create(req, UserContext.getUserId())));
    }

    /**
     * 分页查询评测任务。
     */
    @GetMapping
    public ApiResponse<PageResult<TextToSqlEvalTaskEntity>> page(@Valid TextToSqlEvalTaskPageReq req) {
        return ApiResponse.success(service.page(req));
    }

    /**
     * 查看评测任务详情和汇总信息。
     */
    @GetMapping("/{id}")
    public ApiResponse<TextToSqlEvalTaskDetailRes> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    /**
     * 查看任务下每条样本的执行结果。
     */
    @GetMapping("/{id}/results")
    public ApiResponse<PageResult<TextToSqlEvalResultRes>> results(@PathVariable Long id, @Valid TextToSqlEvalResultPageReq req) {
        return ApiResponse.success(service.results(id, req));
    }

    /**
     * 对中断的评测结果补充澄清回答，并继续执行。
     */
    @PostMapping("/results/{resultId}/continue")
    public ApiResponse<TextToSqlEvalResultRes> continueInterruptedResult(
            @PathVariable Long resultId,
            @Valid @RequestBody TextToSqlEvalContinueReq req
    ) {
        return ApiResponse.success(service.continueInterruptedResult(resultId, req, UserContext.getUserId()));
    }
}
