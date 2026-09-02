package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Text-to-SQL 评测样本详情。
 */
@Data
public class TextToSqlEvalQuestionRes {
    private Long id;
    private String question;
    private String businessDomain;
    private String evalTarget;
    private String sampleCategory;
    private String sourceType;
    private Long sourceRunId;
    private String judgeNote;
    private String remark;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer assertionCount;
    private List<TextToSqlEvalAssertionEntity> assertions;
}
