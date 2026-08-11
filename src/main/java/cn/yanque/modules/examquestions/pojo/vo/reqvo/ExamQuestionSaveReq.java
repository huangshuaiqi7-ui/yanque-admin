package cn.yanque.modules.examquestions.pojo.vo.reqvo;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.Data; import java.util.*;
@Data public class ExamQuestionSaveReq {
    @NotBlank private String questionType;
    @NotBlank private String questionContent;
    @NotBlank private String answerContent;
    private String analysisContent;
    @NotBlank private String difficulty;
    @NotBlank private String status;
    @Valid private List<Option> options=List.of();
    @Valid private List<CourseStage> courseStages=List.of();
    private List<Long> courseIds=List.of();
    @Data public static class Option {
        private Long id; private Long questionId;
        @NotBlank @Size(max=16) private String optionKey;
        @NotBlank private String optionContent;
    }
    @Data public static class CourseStage {
        @NotNull @Positive private Long courseId;
        @NotBlank @Size(max=64) private String stageName;
    }
}
