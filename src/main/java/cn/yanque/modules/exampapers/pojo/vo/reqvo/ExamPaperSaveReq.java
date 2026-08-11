package cn.yanque.modules.exampapers.pojo.vo.reqvo;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.Data; import java.math.BigDecimal; import java.util.List;
@Data public class ExamPaperSaveReq {
    @NotBlank @Size(max=128) private String paperName;
    @NotNull @Positive private Long courseId;
    @Size(max=64) private String stageName;
    @NotNull @DecimalMin(value="0.1") @Digits(integer=9,fraction=1) private BigDecimal totalScore;
    @NotEmpty @Valid private List<Question> questions;
    @Data public static class Question {
        @NotNull @Positive private Long questionId;
        @NotNull @DecimalMin(value="0.1") @Digits(integer=9,fraction=1) private BigDecimal questionScore;
    }
}
