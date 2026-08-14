package cn.yanque.modules.aiknowledge.pojo.vo.resvo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeDocumentRes {
    private Long id;
    private Long knowledgeBaseId;
    private String knowledgeBaseCode;
    private String name;
    private String code;
    private String objectKey;
    private String fileType;
    private Long fileSize;
    private String fileSizeText;
    private String status;
    private String statusText;
    private Integer chunkCount;
    private Integer version;
    private String lastErrorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
