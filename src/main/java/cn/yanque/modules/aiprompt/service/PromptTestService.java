package cn.yanque.modules.aiprompt.service;

import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTestReq;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 提示词测试业务接口。
 */
public interface PromptTestService {
    /**
     * 执行提示词流式测试。
     *
     * @param req 测试请求参数
     * @return 流式测试 SSE
     */
    SseEmitter stream(PromptTestReq req);
}
