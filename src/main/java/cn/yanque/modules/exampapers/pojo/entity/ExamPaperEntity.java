package cn.yanque.modules.exampapers.pojo.entity;
import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class ExamPaperEntity {
    private Long id; private String paperName; private Long courseId; private String stageName;
    private BigDecimal totalScore; private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
