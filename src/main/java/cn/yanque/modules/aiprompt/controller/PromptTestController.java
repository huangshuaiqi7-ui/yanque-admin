package cn.yanque.modules.aiprompt.controller;

import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTestReq;
import cn.yanque.modules.aiprompt.service.PromptTestService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 管理端提示词测试接口。
 */
@RestController
@RequestMapping("/api/ai/prompt-tests")
public class PromptTestController {
    private final PromptTestService service;

    /**
     * 创建提示词测试 Controller。
     *
     * @param service 提示词测试业务服务
     */
    public PromptTestController(PromptTestService service) {
        this.service = service;
    }

    /**
     * 执行提示词流式测试。
     *
     * @param req 测试请求参数
     * @return 流式测试 SSE
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(@Valid @RequestBody PromptTestReq req) {
        return ResponseEntity.ok(service.stream(req));
    }
}
