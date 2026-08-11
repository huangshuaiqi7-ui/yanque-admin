package cn.yanque.modules.studentexams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class ExamSubmissionListRes {
    private Long recordId; private Long examId; private Long studentId; private String studentName; private String studentPhone;
    private String recordStatus; private String recordStatusText; private String gradingStatus; private BigDecimal score;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadlineTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime submitTime;
}
