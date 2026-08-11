package cn.yanque.modules.exampapers.pojo.entity;
import lombok.Data; import java.math.BigDecimal;
@Data public class ExamPaperQuestionEntity {
    private Long id; private Long paperId; private Long questionId; private BigDecimal questionScore;
}
