package cn.yanque.modules.studentexams.pojo.vo.reqvo;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.Data; import java.math.BigDecimal; import java.util.List;
@Data public class ExamGradeReq {
    @NotNull @Valid private List<AnswerGrade> answers;
    @Data public static class AnswerGrade {
        @NotNull @Positive private Long answerId;
        @NotNull @DecimalMin("0.0") @Digits(integer=9,fraction=2) private BigDecimal score;
    }
}
