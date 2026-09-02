package cn.yanque.modules.aitexttosql.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRunEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlAnalyzeReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlFeedbackReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlRunPageReq;
import cn.yanque.modules.aitexttosql.service.TextToSqlAnalysisService;
import cn.yanque.modules.aitexttosql.service.TextToSqlRunService;
import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 Text-to-SQL 数据分析页面接口。
 */
@RestController
@RequestMapping("/api/ai/text-to-sql")
public class TextToSqlAnalysisController {
    private final TextToSqlAnalysisService service;
    private final TextToSqlRunService runService;

    public TextToSqlAnalysisController(TextToSqlAnalysisService service, TextToSqlRunService runService) {
        this.service = service;
        this.runService = runService;
    }

    /**
     * 自然语言数据分析。
     */
    @PostMapping("/analyze")
    public ApiResponse<JSONObject> analyze(@Valid @RequestBody TextToSqlAnalyzeReq req) {
        return ApiResponse.success(service.analyze(req));
    }

    /**
     * 分页查看 Text-to-SQL 运行记录。
     */
    @GetMapping("/runs")
    public ApiResponse<PageResult<TextToSqlRunEntity>> runs(@Valid TextToSqlRunPageReq req) {
        return ApiResponse.success(runService.page(req));
    }

    /**
     * 查看单次运行详情。
     */
    @GetMapping("/runs/{id}")
    public ApiResponse<TextToSqlRunEntity> runDetail(@PathVariable Long id) {
        return ApiResponse.success(runService.detail(id));
    }

    /**
     * 保存 Text-to-SQL 运行结果反馈。
     */
    @PostMapping("/runs/{id}/feedback")
    public ApiResponse<TextToSqlRunEntity> saveFeedback(@PathVariable Long id, @Valid @RequestBody TextToSqlFeedbackReq req) {
        return ApiResponse.success(runService.saveFeedback(id, req));
    }
}
