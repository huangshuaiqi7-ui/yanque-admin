package cn.yanque.modules.aiprompt.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateMapper;
import cn.yanque.modules.aiprompt.mapper.PromptTemplateVersionMapper;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateEntity;
import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateVersionEntity;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateCreateReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplatePageReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateStatusReq;
import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTemplateUpdateReq;
import cn.yanque.modules.aiprompt.pojo.vo.resvo.PromptTemplateRes;
import cn.yanque.modules.aiprompt.service.PromptTemplateService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> PROMPT_TYPES = Set.of("SYSTEM", "USER");
    private static final Set<String> SCENE_CODES = Set.of("CHAT", "RAG", "SUMMARY", "JUDGE", "STRUCTURED_EXTRACT");

    private final PromptTemplateMapper mapper;
    private final PromptTemplateVersionMapper versionMapper;

    /**
     * 创建提示词模板服务实现。
     *
     * @param mapper        提示词模板数据访问对象
     * @param versionMapper 提示词版本数据访问对象
     */
    public PromptTemplateServiceImpl(PromptTemplateMapper mapper,
                                     PromptTemplateVersionMapper versionMapper) {
        this.mapper = mapper;
        this.versionMapper = versionMapper;
    }

    /**
     * 分页查询提示词模板，并把状态文案等展示字段转换为响应对象。
     *
     * @param req 查询条件，支持关键词、Agent、状态和分页参数
     * @return 提示词模板分页结果
     */
    @Override
    public PageResult<PromptTemplateRes> page(PromptTemplatePageReq req) {
        String status = normalizeStatus(req.getStatus(), false);
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<PromptTemplateEntity> rows = mapper.selectPage(
                StrUtil.trim(req.getKeyword()),
                normalizeCode(req.getAgentCode(), false),
                status
        );

        List<PromptTemplateVersionEntity> promptTemplateVersionEntityList = selectActiveVersions(rows);
        PageInfo<PromptTemplateEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(),
                rows.stream().map(promptTemplateEntity -> toRes(promptTemplateEntity, promptTemplateVersionEntityList))
                        .toList());
    }

    /**
     * 按提示词类型查询模板选项。
     *
     * @param promptType 提示词类型，SYSTEM 或 USER
     * @return 模板选项列表
     */
    @Override
    public List<PromptTemplateRes> options(String promptType) {
        List<PromptTemplateEntity> rows = mapper.selectOptions(normalizePromptType(promptType));
        List<PromptTemplateVersionEntity> versions = selectActiveVersions(rows);
        return rows.stream().map(row -> toRes(row, versions)).toList();
    }

    /**
     * 查询提示词模板详情。
     *
     * @param id 提示词模板ID
     * @return 提示词模板详情
     */
    @Override
    public PromptTemplateRes detail(Long id) {
        PromptTemplateEntity promptTemplateEntity = require(id);
        PromptTemplateVersionEntity promptTemplateVersionEntity = versionMapper.selectById(promptTemplateEntity.getId());
        return toRes(require(id), Collections.singletonList(promptTemplateVersionEntity));
    }

    /**
     * 新建提示词模板，编码创建后作为稳定业务标识不允许修改。
     *
     * @param req 新建参数
     */
    @Override
    @Transactional
    public void create(PromptTemplateCreateReq req) {
        PromptTemplateEntity template = buildCreateEntity(req);
        validateUniqueCode(template.getCode());
        try {
            if (mapper.insert(template) != 1) {
                throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_CODE_EXISTS);
        }
    }

    /**
     * 根据新建请求构造可入库的提示词模板实体。
     *
     * @param req 新建参数
     * @return 提示词模板实体
     */
    private PromptTemplateEntity buildCreateEntity(PromptTemplateCreateReq req) {
        PromptTemplateEntity template = new PromptTemplateEntity();
        template.setName(StrUtil.trim(req.getName()));
        template.setCode(normalizeCode(req.getCode(), true));
        template.setAgentCode(normalizeCode(req.getAgentCode(), true));
        template.setPromptType(normalizePromptType(req.getPromptType()));
        template.setSceneCode(normalizeSceneCode(req.getSceneCode()));
        template.setStatus(normalizeStatus(StrUtil.blankToDefault(req.getStatus(), "ACTIVE"), true));
        template.setDescription(StrUtil.trim(req.getDescription()));
        return template;
    }

    /**
     * 编辑提示词模板基础信息，不修改编码和当前启用版本。
     *
     * @param id  提示词模板ID
     * @param req 编辑参数
     */
    @Override
    @Transactional
    public void update(Long id, PromptTemplateUpdateReq req) {
        require(id);
        PromptTemplateEntity template = new PromptTemplateEntity();
        template.setId(id);
        template.setName(StrUtil.trim(req.getName()));
        template.setAgentCode(normalizeCode(req.getAgentCode(), true));
        template.setPromptType(normalizePromptType(req.getPromptType()));
        template.setSceneCode(normalizeSceneCode(req.getSceneCode()));
        template.setDescription(StrUtil.trim(req.getDescription()));
        if (mapper.updateById(template) != 1) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_OPERATION_FAILED);
        }
    }

    /**
     * 启用或禁用提示词模板。
     *
     * @param id  提示词模板ID
     * @param req 状态参数，只允许 ACTIVE 或 INACTIVE
     */
    @Override
    @Transactional
    public void updateStatus(Long id, PromptTemplateStatusReq req) {
        require(id);
        String status = normalizeStatus(req.getStatus(), true);
        if (mapper.updateStatus(id, status) != 1) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_OPERATION_FAILED);
        }
    }

    /**
     * 物理删除提示词模板。
     *
     * @param id 提示词模板ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        require(id);
        if (mapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_OPERATION_FAILED);
        }
    }

    /**
     * 查询提示词模板实体，不存在时抛出业务异常。
     *
     * @param id 提示词模板ID
     * @return 提示词模板实体
     */
    private PromptTemplateEntity require(Long id) {
        PromptTemplateEntity template = mapper.selectById(id);
        if (template == null) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    /**
     * 校验提示词模板编码唯一性。
     *
     * @param code 提示词模板编码
     */
    private void validateUniqueCode(String code) {
        if (mapper.selectByCode(code) != null) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_CODE_EXISTS);
        }
    }

    /**
     * 标准化并校验启用状态。
     *
     * @param value    原始状态值
     * @param required 是否必填
     * @return 标准化后的状态值
     */
    private String normalizeStatus(String value, boolean required) {
        String status = normalize(value);
        if (status == null && !required) {
            return null;
        }
        if (status == null || !STATUSES.contains(status)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_STATUS_INVALID);
        }
        return status;
    }

    /**
     * 标准化并校验提示词类型。
     *
     * @param value 原始提示词类型
     * @return 标准化后的提示词类型
     */
    private String normalizePromptType(String value) {
        String promptType = normalize(value);
        if (promptType == null || !PROMPT_TYPES.contains(promptType)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_TYPE_INVALID);
        }
        return promptType;
    }

    /**
     * 标准化并校验使用场景。
     *
     * @param value 原始场景编码
     * @return 标准化后的场景编码，未填写时返回 null
     */
    private String normalizeSceneCode(String value) {
        String sceneCode = normalize(value);
        if (sceneCode == null) {
            return null;
        }
        if (!SCENE_CODES.contains(sceneCode)) {
            throw BusinessException.of(CommonErrorCode.PROMPT_TEMPLATE_SCENE_INVALID);
        }
        return sceneCode;
    }

    /**
     * 标准化编码类字段。
     *
     * @param value    原始编码
     * @param required 是否必填
     * @return 小写格式的编码，未填写且非必填时返回 null
     */
    private String normalizeCode(String value, boolean required) {
        String code = StrUtil.trim(value);
        if (StrUtil.isBlank(code)) {
            if (required) {
                throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED);
            }
            return null;
        }
        return code.toLowerCase(Locale.ROOT);
    }

    /**
     * 标准化枚举类字段。
     *
     * @param value 原始枚举值
     * @return 大写格式的枚举值，空白值返回 null
     */
    private String normalize(String value) {
        String normalized = StrUtil.trim(value);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * 把提示词模板实体转换为前端响应对象。
     *
     * @param entity 提示词模板实体
     * @return 提示词模板响应对象
     */
    private PromptTemplateRes toRes(PromptTemplateEntity entity, List<PromptTemplateVersionEntity> promptTemplateVersionEntityList) {


        Map<Long, Integer> collect = promptTemplateVersionEntityList.stream().collect(Collectors.toMap(PromptTemplateVersionEntity::getId, PromptTemplateVersionEntity::getVersionNo));


        PromptTemplateRes res = new PromptTemplateRes();
        res.setId(entity.getId());
        res.setCode(entity.getCode());
        res.setName(entity.getName());
        res.setAgentCode(entity.getAgentCode());
        res.setPromptType(entity.getPromptType());
        res.setSceneCode(entity.getSceneCode());
        res.setStatus(entity.getStatus());
        res.setStatusText(CommonStatusEnum.getDescription(entity.getStatus()));
        res.setActiveVersionId(entity.getActiveVersionId());
        res.setActiveVersionNo(collect.get(entity.getActiveVersionId()));
        res.setDescription(entity.getDescription());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }

    /**
     * 批量查询模板当前启用版本。
     *
     * @param rows 提示词模板列表
     * @return 当前启用版本列表
     */
    private List<PromptTemplateVersionEntity> selectActiveVersions(List<PromptTemplateEntity> rows) {
        List<Long> ids = rows.stream()
                .map(PromptTemplateEntity::getActiveVersionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return versionMapper.selectByIds(ids);
    }


}
