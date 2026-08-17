package cn.yanque.modules.aiknowledge.pojo.vo.resvo;

import lombok.Data;

import java.util.List;

@Data
public class AiKnowledgeRecallRes {
    private String query;
    private String mode;
    private Integer topK;
    private List<AiKnowledgeRecallItemRes> records;
}
