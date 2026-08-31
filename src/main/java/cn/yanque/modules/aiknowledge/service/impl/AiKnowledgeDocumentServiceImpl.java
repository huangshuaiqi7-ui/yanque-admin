package cn.yanque.modules.aiknowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.pojo.vo.resvo.PresignDownloadRes;
import cn.yanque.commons.pojo.vo.resvo.PresignUploadRes;
import cn.yanque.commons.service.TosPresignService;
import cn.yanque.modules.aiknowledge.mapper.AiKnowledgeBaseMapper;
import cn.yanque.modules.aiknowledge.mapper.AiKnowledgeDocumentMapper;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeBaseEntity;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeDocumentEntity;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentChunkPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentPresignReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkDetailRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentCreateRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentRes;
import cn.yanque.modules.aiknowledge.service.AiKnowledgeDocumentService;
import cn.yanque.modules.aiknowledge.service.AiKnowledgePythonClient;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
public class AiKnowledgeDocumentServiceImpl implements AiKnowledgeDocumentService {
    private static final Set<String> STATUSES = Set.of("INDEXING", "READY", "FAILED");
    private static final String FILE_TYPE_MD = "md";
    private static final String FILE_TYPE_JSON = "json";
    private static final Set<String> FILE_TYPES = Set.of(FILE_TYPE_MD, FILE_TYPE_JSON);
    private static final Set<String> CHUNK_STRATEGIES = Set.of("MARKDOWN", "NONE", "BY_ITEM");

    private final AiKnowledgeBaseMapper knowledgeBaseMapper;
    private final AiKnowledgeDocumentMapper documentMapper;
    private final TosPresignService tosPresignService;
    private final AiKnowledgePythonClient pythonClient;
    private final Executor documentExecutor;

    public AiKnowledgeDocumentServiceImpl(AiKnowledgeBaseMapper knowledgeBaseMapper,
                                          AiKnowledgeDocumentMapper documentMapper,
                                          TosPresignService tosPresignService,
                                          AiKnowledgePythonClient pythonClient,
                                          @Qualifier("aiKnowledgeDocumentExecutor") Executor documentExecutor) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentMapper = documentMapper;
        this.tosPresignService = tosPresignService;
        this.pythonClient = pythonClient;
        this.documentExecutor = documentExecutor;
    }

    /**
     * 分页查询当前知识库下的文档列表。
     */
    @Override
    public PageResult<AiKnowledgeDocumentRes> page(Long knowledgeBaseId, AiKnowledgeDocumentPageReq req) {
        requireKnowledgeBase(knowledgeBaseId);
        String status = normalizeStatus(req.getStatus(), false);
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<AiKnowledgeDocumentEntity> rows = documentMapper.selectPage(knowledgeBaseId, StrUtil.trim(req.getKeyword()), status);
        PageInfo<AiKnowledgeDocumentEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(), rows.stream().map(this::toRes).toList());
    }

    /**
     * 为当前知识库文档生成 TOS 预签名上传地址。
     */
    @Override
    public PresignUploadRes presignUpload(Long knowledgeBaseId, AiKnowledgeDocumentPresignReq req) {
        AiKnowledgeBaseEntity knowledgeBase = requireKnowledgeBase(knowledgeBaseId);
        String fileName = StrUtil.trim(req.getFileName());
        validateSupportedFileName(fileName);
        String objectKey = buildDocumentObjectKey(knowledgeBase.getCode(), fileName);
        return tosPresignService.presignUpload(objectKey);
    }

    /**
     * 新增文档元数据，并异步触发 Python 切分和向量入库。
     */
    @Override
    public AiKnowledgeDocumentCreateRes create(Long knowledgeBaseId, AiKnowledgeDocumentCreateReq req) {

        // 查询知识库信息
        AiKnowledgeBaseEntity knowledgeBase = requireKnowledgeBase(knowledgeBaseId);

        // 构建文档尸体
        AiKnowledgeDocumentEntity document = buildCreateEntity(knowledgeBase, req);

        // 校验文档是否是md格式 并且查询文档是否存在 如果存在报错
        validateCreateDocument(document);
        try {

            // 入库
            if (documentMapper.insert(document) != 1) {
                throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_OPERATION_FAILED);
        }

        // 开启异步入向量数据库
        submitIndexTask(knowledgeBase.getId(), document.getId());
        return new AiKnowledgeDocumentCreateRes(document.getId(), document.getStatus());
    }

    /**
     * 从 Milvus 分页查询当前文档的 chunk 摘要。
     */
    @Override
    public PageResult<AiKnowledgeDocumentChunkRes> pageChunks(Long knowledgeBaseId,
                                                              Long documentId,
                                                              AiKnowledgeDocumentChunkPageReq req) {
        requireKnowledgeBase(knowledgeBaseId);
        AiKnowledgeDocumentEntity document = requireDocument(knowledgeBaseId, documentId);
        if (!"READY".equals(document.getStatus())) {
            return new PageResult<>(0L, req.getPageNum(), req.getPageSize(), List.of());
        }
        try {
            List<AiKnowledgeDocumentChunkRes> records = pythonClient.queryKnowledgeDocumentChunks(document, req);
            return new PageResult<>(Long.valueOf(document.getChunkCount() == null ? 0 : document.getChunkCount()),
                    req.getPageNum(), req.getPageSize(), records);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_VECTOR_INDEX_FAILED);
        }
    }

    /**
     * 从 Milvus 查询当前文档某个 chunk 的完整内容。
     */
    @Override
    public AiKnowledgeDocumentChunkDetailRes chunkDetail(Long knowledgeBaseId, Long documentId, Integer chunkIndex) {
        requireKnowledgeBase(knowledgeBaseId);
        AiKnowledgeDocumentEntity document = requireDocument(knowledgeBaseId, documentId);
        if (!"READY".equals(document.getStatus())) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_STATUS_INVALID);
        }
        try {
            return pythonClient.getKnowledgeDocumentChunkDetail(document, chunkIndex);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_VECTOR_INDEX_FAILED);
        }
    }

    /**
     * 重新入库当前文档，先把文档状态重置为 INDEXING，再由后台线程覆盖向量。
     */
    @Override
    public void rebuild(Long knowledgeBaseId, Long documentId) {
        requireKnowledgeBase(knowledgeBaseId);
        AiKnowledgeDocumentEntity document = requireDocument(knowledgeBaseId, documentId);
        int nextVersion = document.getVersion() + 1;
        if (documentMapper.markIndexing(documentId, nextVersion) != 1) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_OPERATION_FAILED);
        }
        knowledgeBaseMapper.refreshStatistics(knowledgeBaseId);
        submitIndexTask(knowledgeBaseId, documentId);
    }

    /**
     * 物理删除当前文档，并同步删除 Milvus 中该文档的向量。
     */
    @Override
    public void delete(Long knowledgeBaseId, Long documentId) {
        AiKnowledgeBaseEntity knowledgeBase = requireKnowledgeBase(knowledgeBaseId);
        AiKnowledgeDocumentEntity document = requireDocument(knowledgeBaseId, documentId);
        deleteDocumentVectors(knowledgeBase, document);
        if (documentMapper.deleteById(documentId) != 1) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_OPERATION_FAILED);
        }
        knowledgeBaseMapper.refreshStatistics(knowledgeBaseId);
    }

    private AiKnowledgeDocumentEntity buildCreateEntity(AiKnowledgeBaseEntity knowledgeBase, AiKnowledgeDocumentCreateReq req) {
        AiKnowledgeDocumentEntity document = new AiKnowledgeDocumentEntity();
        document.setKnowledgeBaseId(knowledgeBase.getId());
        document.setKnowledgeBaseCode(knowledgeBase.getCode());
        document.setName(StrUtil.trim(req.getName()));
        document.setCode(StrUtil.trim(req.getCode()).toLowerCase(Locale.ROOT));
        document.setObjectKey(StrUtil.trim(req.getObjectKey()));
        document.setFileType(resolveFileType(document.getObjectKey()));
        document.setChunkStrategy(resolveChunkStrategy(document.getFileType(), req.getChunkStrategy()));
        document.setFileSize(req.getFileSize());
        document.setStatus("INDEXING");
        document.setChunkCount(0);
        document.setVersion(1);
        return document;
    }

    private void validateCreateDocument(AiKnowledgeDocumentEntity document) {
        validateSupportedFileName(document.getName());
        validateDocumentObjectKey(document.getKnowledgeBaseCode(), document.getObjectKey());
        validateChunkStrategy(document.getFileType(), document.getChunkStrategy());
        if (documentMapper.selectByCode(document.getKnowledgeBaseId(), document.getCode()) != null) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_CODE_EXISTS);
        }
        if (documentMapper.selectByName(document.getKnowledgeBaseId(), document.getName()) != null) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_NAME_EXISTS);
        }
    }

    private void validateSupportedFileName(String fileName) {
        if (StrUtil.isBlank(fileName) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
        String fileType = resolveFileType(fileName);
        if (!FILE_TYPES.contains(fileType)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
    }

    private void validateDocumentObjectKey(String knowledgeBaseCode, String objectKey) {
        String prefix = documentObjectKeyPrefix(knowledgeBaseCode);
        if (StrUtil.isBlank(objectKey) || !objectKey.startsWith(prefix)
                || objectKey.contains("..") || objectKey.contains("\\")) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
        if (!FILE_TYPES.contains(resolveFileType(objectKey))) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
    }

    private String buildDocumentObjectKey(String knowledgeBaseCode, String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return documentObjectKeyPrefix(knowledgeBaseCode) + UUID.randomUUID() + suffix;
    }

    private String documentObjectKeyPrefix(String knowledgeBaseCode) {
        return "ai/knowledge/" + knowledgeBaseCode + "/documents/";
    }

    private String resolveFileType(String fileName) {
        String lowerName = StrUtil.trim(fileName).toLowerCase(Locale.ROOT);
        int dotIndex = lowerName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == lowerName.length() - 1) {
            return "";
        }
        return lowerName.substring(dotIndex + 1);
    }

    private String resolveChunkStrategy(String fileType, String requestedStrategy) {
        String strategy = StrUtil.trim(requestedStrategy);
        if (StrUtil.isBlank(strategy)) {
            return FILE_TYPE_JSON.equals(fileType) ? "NONE" : "MARKDOWN";
        }
        return strategy.toUpperCase(Locale.ROOT);
    }

    private void validateChunkStrategy(String fileType, String chunkStrategy) {
        if (!CHUNK_STRATEGIES.contains(chunkStrategy)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
        if (FILE_TYPE_MD.equals(fileType) && "BY_ITEM".equals(chunkStrategy)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
        if (FILE_TYPE_JSON.equals(fileType) && "MARKDOWN".equals(chunkStrategy)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_FILE_INVALID);
        }
    }

    private void submitIndexTask(Long knowledgeBaseId, Long documentId) {
        documentExecutor.execute(() -> indexDocument(knowledgeBaseId, documentId));
    }

    /**
     * 后台执行文档入库，远程 Python 调用不放入数据库事务。
     */
    private void indexDocument(Long knowledgeBaseId, Long documentId) {
        AiKnowledgeBaseEntity knowledgeBase;
        AiKnowledgeDocumentEntity document;
        try {
            knowledgeBase = requireKnowledgeBase(knowledgeBaseId);
            document = requireDocument(knowledgeBaseId, documentId);
        } catch (BusinessException exception) {
            return;
        }
        Integer version = document.getVersion();
        try {
            PresignDownloadRes presign = tosPresignService.presignDownload(document.getObjectKey());
            Integer chunkCount = pythonClient.indexKnowledgeDocument(knowledgeBase, document, presign.getDownloadUrl());
            documentMapper.markReady(documentId, version, chunkCount == null ? 0 : chunkCount);
        } catch (Exception exception) {
            documentMapper.markFailed(documentId, version, trimError(exception));
        } finally {
            knowledgeBaseMapper.refreshStatistics(knowledgeBaseId);
        }
    }

    private void deleteDocumentVectors(AiKnowledgeBaseEntity knowledgeBase, AiKnowledgeDocumentEntity document) {
        try {
            pythonClient.deleteKnowledgeDocumentVectors(knowledgeBase, document);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_VECTOR_DELETE_FAILED);
        }
    }

    private AiKnowledgeBaseEntity requireKnowledgeBase(Long knowledgeBaseId) {
        AiKnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        return knowledgeBase;
    }

    private AiKnowledgeDocumentEntity requireDocument(Long knowledgeBaseId, Long documentId) {
        AiKnowledgeDocumentEntity document = documentMapper.selectByIdAndKnowledgeBaseId(documentId, knowledgeBaseId);
        if (document == null) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_NOT_FOUND);
        }
        return document;
    }

    private String normalizeStatus(String value, boolean required) {
        String status = normalize(value);
        if (status == null && !required) {
            return null;
        }
        if (status == null || !STATUSES.contains(status)) {
            throw BusinessException.of(CommonErrorCode.KNOWLEDGE_DOCUMENT_STATUS_INVALID);
        }
        return status;
    }

    private String normalize(String value) {
        String trimmed = StrUtil.trim(value);
        return StrUtil.isBlank(trimmed) ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimError(Exception exception) {
        String message = exception.getMessage();
        if (StrUtil.isBlank(message)) {
            message = exception.getClass().getSimpleName();
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private AiKnowledgeDocumentRes toRes(AiKnowledgeDocumentEntity document) {
        AiKnowledgeDocumentRes result = new AiKnowledgeDocumentRes();
        result.setId(document.getId());
        result.setKnowledgeBaseId(document.getKnowledgeBaseId());
        result.setKnowledgeBaseCode(document.getKnowledgeBaseCode());
        result.setName(document.getName());
        result.setCode(document.getCode());
        result.setObjectKey(document.getObjectKey());
        result.setFileType(document.getFileType());
        result.setChunkStrategy(document.getChunkStrategy());
        result.setFileSize(document.getFileSize());
        result.setFileSizeText(formatFileSize(document.getFileSize()));
        result.setStatus(document.getStatus());
        result.setStatusText(statusText(document.getStatus()));
        result.setChunkCount(document.getChunkCount());
        result.setVersion(document.getVersion());
        result.setLastErrorMessage(document.getLastErrorMessage());
        result.setCreatedAt(document.getCreatedAt());
        result.setUpdatedAt(document.getUpdatedAt());
        return result;
    }

    private String statusText(String status) {
        if ("INDEXING".equals(status)) {
            return "入库中";
        }
        if ("READY".equals(status)) {
            return "已完成";
        }
        if ("FAILED".equals(status)) {
            return "失败";
        }
        return status;
    }

    private String formatFileSize(Long fileSize) {
        if (fileSize == null) {
            return "-";
        }
        if (fileSize < 1024) {
            return fileSize + "B";
        }
        if (fileSize < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1fKB", fileSize / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1fMB", fileSize / 1024.0 / 1024.0);
    }
}
