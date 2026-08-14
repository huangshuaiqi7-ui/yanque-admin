package cn.yanque.modules.aiknowledge.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeDocumentEntity {
    private Long id;
    private Long knowledgeBaseId;
    /** 冗余知识库编码，Python 侧直接用它定位 Milvus Collection。 */
    private String knowledgeBaseCode;
    private String name;
    private String code;
    private String objectKey;
    private String fileType;
    private Long fileSize;
    /** 入库状态：INDEXING 入库中，READY 已完成，FAILED 失败。 */
    private String status;
    private Integer chunkCount;
    /** 文档版本号，重建时递增，用于区分本次入库批次。 */
    private Integer version;
    private String lastErrorMessage;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
