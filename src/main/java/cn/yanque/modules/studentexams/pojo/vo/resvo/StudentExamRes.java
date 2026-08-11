package cn.yanque.modules.studentexams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class StudentExamRes {
    private Long id; private Long paperId; private String paperName; private Long classId; private String classPeriod;
    private Long courseId; private String stageName; private Integer durationMinutes; private Long invigilatorUserId;
    private String invigilatorName; private BigDecimal totalScore; private Long recordId; private String recordStatus;
    private String gradingStatus; private BigDecimal score; private Boolean answerVisible; private String examStatus; private String examStatusText; private Boolean canStart;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime endTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime recordStartTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadlineTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime submitTime;
}
