package cn.yanque.modules.examquestions.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.time.LocalDateTime; import java.util.*;
@Data public class ExamQuestionRes {
    private Long id; private String questionType; private String questionContent; private String answerContent;
    private String analysisContent; private String difficulty; private String status;
    private List<Long> courseIds=List.of(); private String courseNames;
    private List<String> courseStageKeys=List.of(); private String courseStageNames;
    private List<OptionRes> options=List.of();
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime updatedAt;
    @Data public static class OptionRes { private Long id; private Long questionId; private String optionKey; private String optionContent; }
}
