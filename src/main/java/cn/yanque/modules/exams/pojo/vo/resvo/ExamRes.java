package cn.yanque.modules.exams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.time.LocalDateTime;
@Data public class ExamRes {
    private Long id; private Long paperId; private String paperName; private Long classId; private String classPeriod;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime endTime;
    private Integer durationMinutes; private Long invigilatorUserId; private String invigilatorName; private Boolean answerVisible;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime updatedAt;
}
