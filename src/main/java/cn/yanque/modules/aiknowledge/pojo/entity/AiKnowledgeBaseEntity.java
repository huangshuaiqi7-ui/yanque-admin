package cn.yanque.modules.aiknowledge.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeBaseEntity {
    private Long id;
    private String name;
    /** 稳定业务编码，后续会作为 Python / Milvus 过滤和定位知识库的标识，创建后不允许修改。 */
    private String code;
    private String description;
    /** 业务启用状态，只表示是否参与管理端使用和后续问答召回。 */
    private String status;
    private Integer documentCount;
    private Integer chunkCount;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
