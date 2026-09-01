package cn.yanque.modules.aitexttosql.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlAnalyzeReq;
import cn.yanque.modules.aitexttosql.service.TextToSqlAnalysisService;
import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.Valid;
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

    public TextToSqlAnalysisController(TextToSqlAnalysisService service) {
        this.service = service;
    }

    /**
     * 自然语言数据分析。
     */
    @PostMapping("/analyze")
    public ApiResponse<JSONObject> analyze(@Valid @RequestBody TextToSqlAnalyzeReq req) {
        return ApiResponse.success(service.analyze(req));
    }
}
