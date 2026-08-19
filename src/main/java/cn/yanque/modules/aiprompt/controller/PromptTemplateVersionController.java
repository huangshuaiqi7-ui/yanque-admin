package cn.yanque.modules.aiprompt.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateVersionCreateReq;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.PromptTemplateVersionRes;
import cn.yanque.modules.aiprompt.service.PromptTemplateVersionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端提示词版本接口。
 *
 * 本期负责版本列表、详情、新建、发布和回滚，不处理测试和评估。
 */
@RestController
@RequestMapping("/api/ai/prompt-templates/{templateId}/versions")
public class PromptTemplateVersionController {
    private final PromptTemplateVersionService service;

    /**
     * 创建提示词版本 Controller。
     *
     * @param service 提示词版本业务服务
     */
    public PromptTemplateVersionController(PromptTemplateVersionService service) {
        this.service = service;
    }

    /**
     * 查询指定模板下的提示词版本列表。
     *
     * @param templateId 提示词模板ID
     * @return 提示词版本列表
     */
    @GetMapping
    public ApiResponse<List<PromptTemplateVersionRes>> list(@PathVariable Long templateId) {
        return ApiResponse.success(service.list(templateId));
    }

    /**
     * 查询提示词版本详情。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     * @return 提示词版本详情
     */
    @GetMapping("/{versionId}")
    public ApiResponse<PromptTemplateVersionRes> detail(@PathVariable Long templateId, @PathVariable Long versionId) {
        return ApiResponse.success(service.detail(templateId, versionId));
    }

    /**
     * 新建提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param req        新建版本参数
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<Void> create(@PathVariable Long templateId, @Valid @RequestBody PromptTemplateVersionCreateReq req) {
        service.create(templateId, req);
        return ApiResponse.success();
    }

    /**
     * 发布指定提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     * @return 操作结果
     */
    @PutMapping("/{versionId}/publish")
    public ApiResponse<Void> publish(@PathVariable Long templateId, @PathVariable Long versionId) {
        service.publish(templateId, versionId);
        return ApiResponse.success();
    }

    /**
     * 回滚到指定历史提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     * @return 操作结果
     */
    @PutMapping("/{versionId}/rollback")
    public ApiResponse<Void> rollback(@PathVariable Long templateId, @PathVariable Long versionId) {
        service.rollback(templateId, versionId);
        return ApiResponse.success();
    }

}
