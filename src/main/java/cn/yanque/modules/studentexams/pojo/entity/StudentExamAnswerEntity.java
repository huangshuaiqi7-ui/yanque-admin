package cn.yanque.modules.studentexams.pojo.entity;
import lombok.Data; import java.math.BigDecimal;
@Data public class StudentExamAnswerEntity {
    private Long id; private Long recordId; private Long examId; private Long paperId; private Long questionId;
    private String questionType; private BigDecimal questionScore; private String answerContent;
    private Boolean correct; private BigDecimal score;
}
