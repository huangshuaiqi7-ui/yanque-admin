package cn.yanque.modules.studentexams.pojo.vo.reqvo;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.Data; import java.util.List;
@Data public class StudentExamSubmitReq {
    @NotNull @Valid private List<Answer> answers;
    @Data public static class Answer { @NotNull @Positive private Long questionId; private String answerContent; }
}
