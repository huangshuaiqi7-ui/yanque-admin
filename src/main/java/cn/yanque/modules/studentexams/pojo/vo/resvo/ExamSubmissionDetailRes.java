package cn.yanque.modules.studentexams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.*;
@Data public class ExamSubmissionDetailRes {
    private Long recordId; private Long examId; private Long paperId; private String paperName; private String classPeriod;
    private String stageName; private Long studentId; private String studentName; private String studentPhone;
    private BigDecimal totalScore; private BigDecimal score; private String recordStatus; private String gradingStatus;
    private Boolean answerVisible; private List<Question> questions=List.of();
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadlineTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime submitTime;
    @Data public static class Question {
        private Long id; private Long answerId; private Long questionId; private String questionContent; private String questionType;
        private BigDecimal questionScore; private String answerContent; private String correctAnswer; private String analysisContent;
        private Boolean correct; private BigDecimal score; private List<StudentExamPaperRes.Option> options=List.of();
    }
}
