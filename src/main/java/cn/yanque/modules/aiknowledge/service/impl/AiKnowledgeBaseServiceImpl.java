package cn.yanque.modules.aiknowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aiknowledge.mapper.AiKnowledgeBaseMapper;
import cn.yanque.modules.aiknowledge.mapper.AiKnowledgeDocumentMapper;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeBaseEntity;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBasePageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseStatusReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseUpdateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeQaReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeRecallReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeBaseRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeRecallRes;
import cn.yanque.modules.aiknowledge.service.AiKnowledgeBaseService;
import cn.yanque.modules.aiknowledge.service.AiKnowledgePythonClient;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AiKnowledgeBaseServiceImpl implements AiKnowledgeBaseService {
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> RECALL_MODES = Set.of("SEMANTIC", "KEYWORD", "HYBRID");

    private final AiKnowledgeBaseMapper mapper;
    private final AiKnowledgeDocumentMapper documentMapper;
    private final AiKnowledgePythonClient pythonClient;

    public AiKnowledgeBaseServiceImpl(AiKnowledgeBaseMapper mapper,
                                      AiKnowledgeDocumentMapper documentMapper,
                                      AiKnowledgePythonClient pythonClient) {
        this.mapper = mapper;
        this.documentMapper = documentMapper;
        this.pythonClient = pythonClient;
    }

    @Override
    public PageResult<AiKnowledgeBaseRes> page(AiKnowledgeBasePageReq req) {
        String status = normalizeStatus(req.getStatus(), false);
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<AiKnowledgeBaseEntity> rows = mapper.selectPage(StrUtil.trim(req.getKeyword()), status);
        PageInfo<AiKnowledgeBaseEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(), rows.stream().map(this::toRes).toList());
    }

    @Override
    public AiKnowledgeBaseRes detail(Long id) {
        return toRes(require(id));
    }

    @Override
    public Long create(AiKnowledgeBaseCreateReq req) {
        AiKnowledgeBaseEntity knowledgeBase = insertKnowledgeBase(req);
        try {
            initVectorCollection(knowledgeBase);
        } catch (BusinessException exception) {
            cleanupCreatedKnowledgeBase(knowledgeBase.getId());
            throw exception;
        }
        return knowledgeBase.getId();
    }

    /**
     * 只写 MySQL，不开启长事务；后面的 Python/Milvus 调用在 insert 完成后执行。
     */
    private AiKnowledgeBaseEntity insertKnowledgeBase(AiKnowledgeBaseCreateReq req) {
        AiKnowledgeBaseEntity knowledgeBase = buildCreateEntity(req);
        validateUniqueCode(knowledgeBase.getCode());
        try {
            if (mapper.insert(knowledgeBase) != 1) {
                throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_CODE_EXISTS);
        }
        return knowledgeBase;
    }

    /**
     * Python/Milvus 初始化失败后的补偿清理，删除刚插入的知识库记录。
     */
    private void cleanupCreatedKnowledgeBase(Long id) {
        mapper.deleteById(id);
    }

    private AiKnowledgeBaseEntity buildCreateEntity(AiKnowledgeBaseCreateReq req) {
        String code = StrUtil.trim(req.getCode()).toLowerCase(Locale.ROOT);
        AiKnowledgeBaseEntity knowledgeBase = new AiKnowledgeBaseEntity();
        knowledgeBase.setName(StrUtil.trim(req.getName()));
        knowledgeBase.setCode(code);
        knowledgeBase.setDescription(StrUtil.trim(req.getDescription()));
        knowledgeBase.setStatus("ACTIVE");
        knowledgeBase.setDocumentCount(0);
        knowledgeBase.setChunkCount(0);
        return knowledgeBase;
    }

    @Override
    @Transactional
    public void update(Long id, AiKnowledgeBaseUpdateReq req) {
        require(id);
        AiKnowledgeBaseEntity knowledgeBase = new AiKnowledgeBaseEntity();
        knowledgeBase.setId(id);
        knowledgeBase.setName(StrUtil.trim(req.getName()));
        knowledgeBase.setDescription(StrUtil.trim(req.getDescription()));
        if (mapper.updateById(knowledgeBase) != 1) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_OPERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, AiKnowledgeBaseStatusReq req) {
        require(id);
        String status = normalizeStatus(req.getStatus(), true);
        if (mapper.updateStatus(id, status) != 1) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_OPERATION_FAILED);
        }
    }

    @Override
    public AiKnowledgeRecallRes recall(Long id, AiKnowledgeRecallReq req) {
        AiKnowledgeBaseEntity knowledgeBase = require(id);
        if (!"ACTIVE".equals(knowledgeBase.getStatus())) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_STATUS_INVALID);
        }
        req.setMode(normalizeRecallMode(req.getMode()));
        req.setQuery(StrUtil.trim(req.getQuery()));
        req.setTopK(normalizeTopK(req.getTopK()));
        try {
            return pythonClient.recallKnowledgeBase(knowledgeBase, req);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_RECALL_FAILED);
        }
    }

    @Override
    public SseEmitter qa(Long id, AiKnowledgeQaReq req) {
        AiKnowledgeBaseEntity knowledgeBase = require(id);
        if (!"ACTIVE".equals(knowledgeBase.getStatus())) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_STATUS_INVALID);
        }
        req.setRecallMode(normalizeRecallMode(req.getRecallMode()));
        req.setQuestion(StrUtil.trim(req.getQuestion()));
        req.setTopK(normalizeTopK(req.getTopK()));

        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> streamQa(emitter, knowledgeBase, req)).start();
        return emitter;
    }

    /**
     * 调用 Python 知识库问答流式接口，并把 SSE 事件原样转给前端。
     */
    private void streamQa(SseEmitter emitter, AiKnowledgeBaseEntity knowledgeBase, AiKnowledgeQaReq req) {
        AtomicBoolean finished = new AtomicBoolean(false);
        try {
            pythonClient.streamKnowledgeBaseQa(knowledgeBase, req, event -> {
                JSONObject data = event.data();
                sendEvent(emitter, event.event(), data);
                if ("done".equals(event.event()) || "error".equals(event.event())) {
                    finished.set(true);
                    emitter.complete();
                }
            });
            if (!finished.get()) {
                sendEvent(emitter, "error", Map.of("message", "AI知识库问答响应异常，请稍后重试"));
                emitter.complete();
            }
        } catch (Exception exception) {
            sendEvent(emitter, "error", Map.of("message", "AI知识库问答暂时不可用，请稍后重试"));
            emitter.complete();
        }
    }

    /** 发送一条 SSE 事件给前端。 */
    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(JSON.toJSONString(data == null ? new LinkedHashMap<>() : data)));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public void delete(Long id) {
        AiKnowledgeBaseEntity knowledgeBase = require(id);
        deleteVectorCollection(knowledgeBase);
        documentMapper.deleteByKnowledgeBaseId(id);
        if (mapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_OPERATION_FAILED);
        }
    }

    private AiKnowledgeBaseEntity require(Long id) {
        AiKnowledgeBaseEntity knowledgeBase = mapper.selectById(id);
        if (knowledgeBase == null) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        return knowledgeBase;
    }

    private void validateUniqueCode(String code) {
        if (mapper.selectByCode(code) != null) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_CODE_EXISTS);
        }
    }

    /**
     * 初始化 Milvus Collection。
     *
     * 该方法不在数据库事务内；如果 Python 或 Milvus 初始化失败，会抛出业务异常并触发补偿删除。
     */
    private void initVectorCollection(AiKnowledgeBaseEntity knowledgeBase) {
        try {
            pythonClient.createKnowledgeBaseCollection(knowledgeBase);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_VECTOR_INIT_FAILED);
        }
    }

    /**
     * 删除 Milvus Collection。
     *
     * 先删除向量库，再物理删除 MySQL；如果向量库删除失败，保留 MySQL 记录方便管理员重试。
     */
    private void deleteVectorCollection(AiKnowledgeBaseEntity knowledgeBase) {
        try {
            pythonClient.deleteKnowledgeBaseCollection(knowledgeBase);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_VECTOR_DELETE_FAILED);
        }
    }

    private String normalizeStatus(String value, boolean required) {
        String status = normalize(value);
        if (status == null && !required) {
            return null;
        }
        if (status == null || !STATUSES.contains(status)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_STATUS_INVALID);
        }
        return status;
    }

    private String normalize(String value) {
        String trimmed = StrUtil.trim(value);
        return StrUtil.isBlank(trimmed) ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String normalizeRecallMode(String value) {
        String mode = normalize(value);
        if (mode == null || !RECALL_MODES.contains(mode)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_RECALL_MODE_INVALID);
        }
        return mode;
    }

    private Integer normalizeTopK(Integer value) {
        if (value == null) {
            return 5;
        }
        if (value < 1 || value > 20) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED);
        }
        return value;
    }

    private AiKnowledgeBaseRes toRes(AiKnowledgeBaseEntity knowledgeBase) {
        AiKnowledgeBaseRes result = new AiKnowledgeBaseRes();
        result.setId(knowledgeBase.getId());
        result.setName(knowledgeBase.getName());
        result.setCode(knowledgeBase.getCode());
        result.setDescription(knowledgeBase.getDescription());
        result.setDocumentCount(knowledgeBase.getDocumentCount());
        result.setChunkCount(knowledgeBase.getChunkCount());
        result.setStatus(knowledgeBase.getStatus());
        result.setStatusText(CommonStatusEnum.getDescription(knowledgeBase.getStatus()));
        result.setCreatedAt(knowledgeBase.getCreatedAt());
        result.setUpdatedAt(knowledgeBase.getUpdatedAt());
        return result;
    }
}
