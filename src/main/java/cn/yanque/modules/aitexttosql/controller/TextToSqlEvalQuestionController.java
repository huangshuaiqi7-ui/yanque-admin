package cn.yanque.modules.aitexttosql.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.context.UserContext;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalQuestionPageReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalQuestionSaveReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlEvalQuestionRes;
import cn.yanque.modules.aitexttosql.service.TextToSqlEvalQuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端 Text-to-SQL 评测样本接口。
 */
@RestController
@RequestMapping("/api/ai/text-to-sql/eval-questions")
public class TextToSqlEvalQuestionController {
    private final TextToSqlEvalQuestionService service;

    public TextToSqlEvalQuestionController(TextToSqlEvalQuestionService service) {
        this.service = service;
    }

    /**
     * 分页查询评测样本。
     */
    @GetMapping
    public ApiResponse<PageResult<TextToSqlEvalQuestionRes>> page(@Valid TextToSqlEvalQuestionPageReq req) {
        return ApiResponse.success(service.page(req));
    }

    /**
     * 查看评测样本详情，包含断言列表。
     */
    @GetMapping("/{id}")
    public ApiResponse<TextToSqlEvalQuestionRes> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    /**
     * 手动录入评测样本。
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody TextToSqlEvalQuestionSaveReq req) {
        return ApiResponse.success(Map.of("id", service.create(req, UserContext.getUserId())));
    }

    /**
     * 修改评测样本和它的断言标准。
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TextToSqlEvalQuestionSaveReq req) {
        service.update(id, req);
        return ApiResponse.success();
    }

    /**
     * 从运行记录生成草稿样本。
     */
    @PostMapping("/from-run/{runId}")
    public ApiResponse<Map<String, Long>> createFromRun(@PathVariable Long runId) {
        return ApiResponse.success(Map.of("id", service.createFromRun(runId, UserContext.getUserId())));
    }
}
