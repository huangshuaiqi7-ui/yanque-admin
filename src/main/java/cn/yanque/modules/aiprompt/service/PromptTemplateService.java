package cn.yanque.modules.aiprompt.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateCreateReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplatePageReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateStatusReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateUpdateReq;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.PromptTemplateRes;

public interface PromptTemplateService {
    /**
     * 分页查询提示词模板。
     *
     * @param req 查询条件，支持关键词、Agent、状态和分页参数
     * @return 提示词模板分页结果
     */
    PageResult<PromptTemplateRes> page(PromptTemplatePageReq req);

    /**
     * 查询提示词模板详情。
     *
     * @param id 提示词模板ID
     * @return 提示词模板详情
     */
    PromptTemplateRes detail(Long id);

    /**
     * 新建提示词模板。
     *
     * @param req 新建参数
     */
    void create(PromptTemplateCreateReq req);

    /**
     * 编辑提示词模板基础信息。
     *
     * @param id  提示词模板ID
     * @param req 编辑参数，不允许修改编码和当前启用版本
     */
    void update(Long id, PromptTemplateUpdateReq req);

    /**
     * 启用或禁用提示词模板。
     *
     * @param id  提示词模板ID
     * @param req 状态参数，只允许 ACTIVE 或 INACTIVE
     */
    void updateStatus(Long id, PromptTemplateStatusReq req);

    /**
     * 物理删除提示词模板。
     *
     * @param id 提示词模板ID
     */
    void delete(Long id);
}
