package cn.yanque.modules.studentexams.pojo.entity;
import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class StudentExamRecordEntity {
    private Long id; private Long examId; private Long studentId; private LocalDateTime startTime;
    private LocalDateTime deadlineTime; private LocalDateTime submitTime; private String status;
    private String gradingStatus; private BigDecimal score; private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
