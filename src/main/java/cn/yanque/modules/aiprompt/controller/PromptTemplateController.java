package cn.yanque.modules.aiprompt.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateCreateReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplatePageReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateStatusReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateUpdateReq;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.PromptTemplateRes;
import cn.yanque.modules.aiprompt.service.PromptTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端提示词模板接口。
 *
 * 本期只维护模板元信息；提示词正文、版本发布、测试评估后续放到版本管理接口。
 */
@RestController
@RequestMapping("/api/ai/prompt-templates")
public class PromptTemplateController {
    private final PromptTemplateService service;

    /**
     * 创建提示词模板 Controller。
     *
     * @param service 提示词模板业务服务
     */
    public PromptTemplateController(PromptTemplateService service) {
        this.service = service;
    }

    /**
     * 分页查询提示词模板。
     *
     * @param req 查询条件，支持关键词、Agent、状态和分页参数
     * @return 提示词模板分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<PromptTemplateRes>> page(@Valid PromptTemplatePageReq req) {
        return ApiResponse.success(service.page(req));
    }

    /**
     * 查询提示词模板详情。
     *
     * @param id 提示词模板ID
     * @return 提示词模板详情
     */
    @GetMapping("/{id}")
    public ApiResponse<PromptTemplateRes> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    /**
     * 新建提示词模板。
     *
     * @param req 新建参数，包含编码、名称、Agent、类型、场景和说明
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody PromptTemplateCreateReq req) {
        service.create(req);
        return ApiResponse.success();
    }

    /**
     * 编辑提示词模板基础信息。
     *
     * @param id  提示词模板ID
     * @param req 编辑参数，不允许修改编码和当前启用版本
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PromptTemplateUpdateReq req) {
        service.update(id, req);
        return ApiResponse.success();
    }

    /**
     * 启用或禁用提示词模板。
     *
     * @param id  提示词模板ID
     * @param req 状态参数，只允许 ACTIVE 或 INACTIVE
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PromptTemplateStatusReq req) {
        service.updateStatus(id, req);
        return ApiResponse.success();
    }

    /**
     * 物理删除提示词模板。
     *
     * @param id 提示词模板ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
