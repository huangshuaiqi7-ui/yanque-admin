package cn.yanque.modules.exams.pojo.vo.reqvo;
import com.fasterxml.jackson.annotation.JsonFormat; import jakarta.validation.constraints.*; import lombok.Data; import java.time.LocalDateTime;
@Data public class ExamSaveReq {
    @NotNull @Positive private Long paperId;
    @NotNull @Positive private Long classId;
    @NotNull @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @NotNull @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime endTime;
    @NotNull @Min(1) private Integer durationMinutes;
    @NotNull @Positive private Long invigilatorUserId;
}
