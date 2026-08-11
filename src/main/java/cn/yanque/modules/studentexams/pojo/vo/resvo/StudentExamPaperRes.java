package cn.yanque.modules.studentexams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.*;
@Data public class StudentExamPaperRes {
    private Long recordId; private Long examId; private Long paperId; private String paperName; private Long classId;
    private String classPeriod; private Long courseId; private String stageName; private BigDecimal totalScore;
    private String recordStatus; private String gradingStatus; private List<Question> questions=List.of();
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadlineTime;
    @Data public static class Question {
        private Long id; private Long questionId; private String questionContent; private String questionType;
        private String difficulty; private BigDecimal questionScore; private List<Option> options=List.of();
    }
    @Data public static class Option { private Long id; private Long questionId; private String optionKey; private String optionContent; }
}
