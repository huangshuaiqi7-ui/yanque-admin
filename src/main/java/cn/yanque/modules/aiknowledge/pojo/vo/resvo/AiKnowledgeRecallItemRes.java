package cn.yanque.modules.aiknowledge.pojo.vo.resvo;

import lombok.Data;

@Data
public class AiKnowledgeRecallItemRes {
    private Integer rank;
    private Double score;
    private String documentCode;
    private Integer documentVersion;
    private Integer chunkIndex;
    private String content;
    private String contentPreview;
    private Integer wordCount;
}
