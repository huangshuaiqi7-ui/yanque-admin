package cn.yanque.modules.aiprompt.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateMapper;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateVersionMapper;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateEntity;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateVersionEntity;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.InternalPromptRes;
import cn.yanque.modules.aiprompt.service.InternalPromptService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 内部服务读取提示词业务实现。
 */
@Service
public class InternalPromptServiceImpl implements InternalPromptService {
    private final PromptTemplateMapper templateMapper;
    private final PromptTemplateVersionMapper versionMapper;

    /**
     * 创建内部提示词服务。
     *
     * @param templateMapper 提示词模板数据访问对象
     * @param versionMapper  提示词版本数据访问对象
     */
    public InternalPromptServiceImpl(PromptTemplateMapper templateMapper,
                                     PromptTemplateVersionMapper versionMapper) {
        this.templateMapper = templateMapper;
        this.versionMapper = versionMapper;
    }

    /**
     * 按提示词编码查询当前启用版本。
     *
     * @param code 提示词编码
     * @return 当前启用提示词
     */
    @Override
    public InternalPromptRes getActivePrompt(String code) {
        if (StrUtil.isBlank(code)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }
        PromptTemplateEntity template = templateMapper.selectByCode(code.trim());
        if (template == null || !"ACTIVE".equals(template.getStatus())) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }
        if (template.getActiveVersionId() == null) {
            throw BusinessException.of(CommonErrorCode.PROMPT_ACTIVE_VERSION_NOT_FOUND);
        }
        PromptTemplateVersionEntity version = versionMapper.selectById(template.getActiveVersionId());
        if (version == null || !Objects.equals(version.getTemplateId(), template.getId())
                || StrUtil.isBlank(version.getContent())) {
            throw BusinessException.of(CommonErrorCode.PROMPT_ACTIVE_VERSION_NOT_FOUND);
        }
        return toRes(template, version);
    }

    /**
     * 组装内部提示词响应。
     *
     * @param template 提示词模板实体
     * @param version  当前启用版本实体
     * @return 内部提示词响应
     */
    private InternalPromptRes toRes(PromptTemplateEntity template, PromptTemplateVersionEntity version) {
        InternalPromptRes res = new InternalPromptRes();
        res.setCode(template.getCode());
        res.setName(template.getName());
        res.setAgentCode(template.getAgentCode());
        res.setPromptType(template.getPromptType());
        res.setSceneCode(template.getSceneCode());
        res.setVersionId(version.getId());
        res.setVersionNo(version.getVersionNo());
        res.setContent(version.getContent());
        res.setVariables(version.getVariables());
        res.setUpdateTime(version.getUpdateTime());
        return res;
    }
}
