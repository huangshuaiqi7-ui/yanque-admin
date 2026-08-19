package cn.yanque.modules.aiprompt.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateMapper;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateVersionMapper;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateEntity;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateVersionEntity;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateVersionCreateReq;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.PromptTemplateVersionRes;
import cn.yanque.modules.aiprompt.service.PromptTemplateVersionService;
import cn.yanque.modules.users.mapper.SysUserMapper;
import cn.yanque.modules.users.pojo.entity.SysUserEntity;
import com.alibaba.fastjson2.JSON;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PromptTemplateVersionServiceImpl implements PromptTemplateVersionService {
    private final PromptTemplateMapper templateMapper;
    private final PromptTemplateVersionMapper versionMapper;
    private final SysUserMapper userMapper;

    /**
     * 创建提示词版本服务实现。
     *
     * @param templateMapper 提示词模板数据访问对象
     * @param versionMapper  提示词版本数据访问对象
     * @param userMapper     系统用户数据访问对象
     */
    public PromptTemplateVersionServiceImpl(PromptTemplateMapper templateMapper,
                                            PromptTemplateVersionMapper versionMapper,
                                            SysUserMapper userMapper) {
        this.templateMapper = templateMapper;
        this.versionMapper = versionMapper;
        this.userMapper = userMapper;
    }

    /**
     * 查询指定提示词模板下的版本列表。
     *
     * @param templateId 提示词模板ID
     * @return 提示词版本列表
     */
    @Override
    public List<PromptTemplateVersionRes> list(Long templateId) {
        PromptTemplateEntity template = requireTemplate(templateId);
        PromptTemplateVersionEntity activeVersion = findActiveVersion(template);
        Map<Long, String> userNameCache = new HashMap<>();
        return versionMapper.selectByTemplateId(templateId).stream()
                .map(version -> toRes(version, template.getActiveVersionId(), activeVersion, userNameCache))
                .toList();
    }

    /**
     * 查询提示词版本详情。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     * @return 提示词版本详情
     */
    @Override
    public PromptTemplateVersionRes detail(Long templateId, Long versionId) {
        PromptTemplateEntity template = requireTemplate(templateId);
        PromptTemplateVersionEntity activeVersion = findActiveVersion(template);
        PromptTemplateVersionEntity version = requireVersion(templateId, versionId);
        return toRes(version, template.getActiveVersionId(), activeVersion, new HashMap<>());
    }

    /**
     * 新建提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param req        新建版本参数
     */
    @Override
    @Transactional
    public void create(Long templateId, PromptTemplateVersionCreateReq req) {
        PromptTemplateEntity template = requireTemplate(templateId);
        PromptTemplateVersionEntity version = buildCreateEntity(templateId, req);
        insertVersion(version);
        activateFirstVersionIfNeeded(template, version);
    }

    /**
     * 发布指定提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     */
    @Override
    @Transactional
    public void publish(Long templateId, Long versionId) {
        PromptTemplateEntity template = requireTemplate(templateId);
        PromptTemplateVersionEntity version = requireVersion(templateId, versionId);
        updateActiveVersion(template.getId(), version.getId());
    }

    /**
     * 回滚到指定历史提示词版本。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     */
    @Override
    @Transactional
    public void rollback(Long templateId, Long versionId) {
        PromptTemplateEntity template = requireTemplate(templateId);
        PromptTemplateVersionEntity activeVersion = findActiveVersion(template);
        PromptTemplateVersionEntity targetVersion = requireVersion(templateId, versionId);
        if (activeVersion == null || targetVersion.getVersionNo() >= activeVersion.getVersionNo()) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_OPERATION_FAILED);
        }
        updateActiveVersion(template.getId(), targetVersion.getId());
    }

    /**
     * 根据新建请求构造版本实体。
     *
     * @param templateId 提示词模板ID
     * @param req        新建版本参数
     * @return 提示词版本实体
     */
    private PromptTemplateVersionEntity buildCreateEntity(Long templateId, PromptTemplateVersionCreateReq req) {
        String content = StrUtil.trim(req.getContent());
        if (StrUtil.isBlank(content)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_CONTENT_EMPTY);
        }
        PromptTemplateVersionEntity version = new PromptTemplateVersionEntity();
        version.setTemplateId(templateId);
        version.setVersionNo(nextVersionNo(templateId));
        version.setContent(content);
        version.setVariables(normalizeVariables(req.getVariables()));
        version.setChangeNote(StrUtil.trim(req.getChangeNote()));
        version.setCreateBy(currentUserId());
        return version;
    }

    /**
     * 插入提示词版本。
     *
     * @param version 提示词版本实体
     */
    private void insertVersion(PromptTemplateVersionEntity version) {
        try {
            if (versionMapper.insert(version) != 1) {
                throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_OPERATION_FAILED);
        }
    }

    /**
     * 当模板还没有当前版本且新建的是第一个版本时，自动设为当前版本。
     *
     * @param template 提示词模板实体
     * @param version  新建出的提示词版本实体
     */
    private void activateFirstVersionIfNeeded(PromptTemplateEntity template, PromptTemplateVersionEntity version) {
        if (template.getActiveVersionId() == null && Integer.valueOf(1).equals(version.getVersionNo())) {
            updateActiveVersion(template.getId(), version.getId());
        }
    }

    /**
     * 更新模板当前启用版本。
     *
     * @param templateId       提示词模板ID
     * @param activeVersionId 当前启用版本ID
     */
    private void updateActiveVersion(Long templateId, Long activeVersionId) {
        if (templateMapper.updateActiveVersionId(templateId, activeVersionId) != 1) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_OPERATION_FAILED);
        }
    }

    /**
     * 计算指定模板的下一个版本号。
     *
     * @param templateId 提示词模板ID
     * @return 下一个版本号
     */
    private Integer nextVersionNo(Long templateId) {
        Integer maxVersionNo = versionMapper.selectMaxVersionNo(templateId);
        return maxVersionNo == null ? 1 : maxVersionNo + 1;
    }

    /**
     * 查询提示词模板，不存在时抛出业务异常。
     *
     * @param templateId 提示词模板ID
     * @return 提示词模板实体
     */
    private PromptTemplateEntity requireTemplate(Long templateId) {
        PromptTemplateEntity template = templateMapper.selectById(templateId);
        if (template == null) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    /**
     * 查询提示词版本，并校验版本属于指定模板。
     *
     * @param templateId 提示词模板ID
     * @param versionId  提示词版本ID
     * @return 提示词版本实体
     */
    private PromptTemplateVersionEntity requireVersion(Long templateId, Long versionId) {
        PromptTemplateVersionEntity version = versionMapper.selectById(versionId);
        if (version == null || !Objects.equals(version.getTemplateId(), templateId)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_NOT_FOUND);
        }
        return version;
    }

    /**
     * 查询模板当前启用版本。
     *
     * @param template 提示词模板实体
     * @return 当前启用版本实体，没有当前版本或当前版本不存在时返回 null
     */
    private PromptTemplateVersionEntity findActiveVersion(PromptTemplateEntity template) {
        if (template.getActiveVersionId() == null) {
            return null;
        }
        PromptTemplateVersionEntity activeVersion = versionMapper.selectById(template.getActiveVersionId());
        if (activeVersion == null || !Objects.equals(activeVersion.getTemplateId(), template.getId())) {
            return null;
        }
        return activeVersion;
    }

    /**
     * 标准化并校验变量说明 JSON。
     *
     * @param value 原始变量说明
     * @return 合法 JSON 字符串，空白值返回 null
     */
    private String normalizeVariables(String value) {
        String variables = StrUtil.trim(value);
        if (StrUtil.isBlank(variables)) {
            return null;
        }
        if (!JSON.isValid(variables)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_VERSION_VARIABLES_INVALID);
        }
        return variables;
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 当前登录用户ID，无法获取时返回 null
     */
    private Long currentUserId() {
        return UserContext.getUserId();
    }

    /**
     * 根据用户ID获取展示名称。
     *
     * @param userId        用户ID
     * @param userNameCache 用户名缓存
     * @return 用户展示名称，无法获取时返回 null
     */
    private String resolveUserName(Long userId, Map<Long, String> userNameCache) {
        if (userId == null) {
            return null;
        }
        if (userNameCache.containsKey(userId)) {
            return userNameCache.get(userId);
        }
        SysUserEntity user = userMapper.selectById(userId);
        String name = null;
        if (user != null) {
            name = StrUtil.blankToDefault(user.getRealName(), StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
        }
        userNameCache.put(userId, name);
        return name;
    }

    /**
     * 把提示词版本实体转换为前端响应对象。
     *
     * @param entity          提示词版本实体
     * @param activeVersionId 当前启用版本ID
     * @param activeVersion   当前启用版本实体
     * @param userNameCache   用户名缓存
     * @return 提示词版本响应对象
     */
    private PromptTemplateVersionRes toRes(PromptTemplateVersionEntity entity,
                                           Long activeVersionId,
                                           PromptTemplateVersionEntity activeVersion,
                                           Map<Long, String> userNameCache) {
        String status = resolveVersionStatus(entity, activeVersionId, activeVersion);
        PromptTemplateVersionRes res = new PromptTemplateVersionRes();
        res.setId(entity.getId());
        res.setTemplateId(entity.getTemplateId());
        res.setVersionNo(entity.getVersionNo());
        res.setContent(entity.getContent());
        res.setVariables(entity.getVariables());
        res.setChangeNote(entity.getChangeNote());
        res.setCreateBy(entity.getCreateBy());
        res.setCreateByName(resolveUserName(entity.getCreateBy(), userNameCache));
        res.setCurrent(Objects.equals(entity.getId(), activeVersionId));
        res.setStatus(status);
        res.setStatusText(versionStatusText(status));
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }

    /**
     * 计算提示词版本状态。
     *
     * @param entity          提示词版本实体
     * @param activeVersionId 当前启用版本ID
     * @param activeVersion   当前启用版本实体
     * @return 版本状态
     */
    private String resolveVersionStatus(PromptTemplateVersionEntity entity, Long activeVersionId, PromptTemplateVersionEntity activeVersion) {
        if (Objects.equals(entity.getId(), activeVersionId)) {
            return "CURRENT";
        }
        if (activeVersion == null || entity.getVersionNo() > activeVersion.getVersionNo()) {
            return "UNPUBLISHED";
        }
        return "HISTORY";
    }

    /**
     * 获取版本状态中文文案。
     *
     * @param status 版本状态
     * @return 中文文案
     */
    private String versionStatusText(String status) {
        if ("CURRENT".equals(status)) {
            return "当前";
        }
        if ("UNPUBLISHED".equals(status)) {
            return "未发布";
        }
        return "历史";
    }
}
