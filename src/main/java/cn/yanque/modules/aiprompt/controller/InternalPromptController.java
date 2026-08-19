package cn.yanque.modules.aiprompt.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.InternalPromptRes;
import cn.yanque.modules.aiprompt.service.InternalPromptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部服务提示词查询接口。
 *
 * 路径以 /internal 开头，只供服务间调用，不走 JWT、权限和验签。
 */
@RestController
@RequestMapping("/internal/ai/prompts")
public class InternalPromptController {
    private final InternalPromptService service;

    /**
     * 创建内部提示词 Controller。
     *
     * @param service 内部提示词业务服务
     */
    public InternalPromptController(InternalPromptService service) {
        this.service = service;
    }

    /**
     * 按提示词编码查询当前启用版本。
     *
     * @param code 提示词编码
     * @return 当前启用提示词
     */
    @GetMapping("/active")
    public ApiResponse<InternalPromptRes> active(@RequestParam String code) {
        return ApiResponse.success(service.getActivePrompt(code));
    }
}
