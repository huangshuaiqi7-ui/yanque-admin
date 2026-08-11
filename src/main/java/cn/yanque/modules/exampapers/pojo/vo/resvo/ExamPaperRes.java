package cn.yanque.modules.exampapers.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.*;
@Data public class ExamPaperRes {
    private Long id; private String paperName; private Long courseId; private String courseName; private String stageName;
    private BigDecimal totalScore; private Integer questionCount; private List<QuestionRes> questions=List.of();
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime updatedAt;
    @Data public static class QuestionRes {
        private Long id; private Long paperId; private Long questionId; private String questionContent;
        private String questionType; private String difficulty; private BigDecimal questionScore;
    }
}
