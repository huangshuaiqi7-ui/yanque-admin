package cn.yanque.modules.exams.pojo.entity;
import lombok.Data; import java.time.LocalDateTime;
@Data public class ExamEntity {
    private Long id; private Long paperId; private Long classId;
    private LocalDateTime startTime; private LocalDateTime endTime; private Integer durationMinutes;
    private Long invigilatorUserId; private Boolean answerVisible;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
