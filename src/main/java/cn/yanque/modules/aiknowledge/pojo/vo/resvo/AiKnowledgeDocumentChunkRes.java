package cn.yanque.modules.aiknowledge.pojo.vo.resvo;

import lombok.Data;

@Data
public class AiKnowledgeDocumentChunkRes {
    private Integer chunkIndex;
    private String contentPreview;
    private Integer wordCount;
    private Integer charCount;
}
