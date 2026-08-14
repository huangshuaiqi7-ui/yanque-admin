package cn.yanque.modules.aiknowledge.pojo.vo.resvo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeBaseRes {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer documentCount;
    private Integer chunkCount;
    private String status;
    private String statusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
