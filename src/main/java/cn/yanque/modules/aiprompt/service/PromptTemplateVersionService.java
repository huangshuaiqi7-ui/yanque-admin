package cn.yanque.modules.aiprompt.service;

import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateVersionCreateReq;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.PromptTemplateVersionRes;

import java.util.List;

public interface PromptTemplateVersionService {
    /**
     * 查询指定提示词模板下的版本列表。
     *
     * @param templateId 提示词模板ID
     * @return 提示词版本列表
     */
    List<PromptTemplateVersionRes> list(Long templateId);

    /**
     * 查询提示词版本详情。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     * @return 提示词版本详情
     */
    PromptTemplateVersionRes detail(Long templateId, Long versionId);

    /**
     * 新建提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param req        新建版本参数
     */
    void create(Long templateId, PromptTemplateVersionCreateReq req);

    /**
     * 发布指定提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     */
    void publish(Long templateId, Long versionId);

    /**
     * 回滚到指定历史提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     */
    void rollback(Long templateId, Long versionId);

}
