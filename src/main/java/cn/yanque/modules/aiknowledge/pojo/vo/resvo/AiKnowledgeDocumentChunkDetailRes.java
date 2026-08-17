package cn.yanque.modules.aiknowledge.pojo.vo.resvo;

import lombok.Data;

@Data
public class AiKnowledgeDocumentChunkDetailRes {
    private Integer chunkIndex;
    private String content;
    private Integer wordCount;
    private Integer charCount;
}
