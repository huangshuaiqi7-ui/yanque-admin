package cn.yanque.modules.studentexams.pojo.vo.resvo;
import lombok.Data; import java.math.BigDecimal;
@Data public class ExamPaperQuestionRow {
    private Long id; private Long paperId; private Long questionId; private String questionContent; private String questionType;
    private String difficulty; private String correctAnswer; private String analysisContent; private BigDecimal questionScore;
}
