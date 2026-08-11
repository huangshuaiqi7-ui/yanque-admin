package cn.yanque.modules.homeworks.pojo.vo.reqvo;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.*;
@Data public class HomeworkCreateReq {
    @NotBlank @Size(max=100) private String title;
    @NotBlank @Size(max=500) private String contentObjectKey;
    @NotBlank @Size(max=255) private String contentFileName;
    @NotNull @Positive private Long classId;
    @NotNull private LocalDate homeworkDate;
    private String classContent;
    @NotNull @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @NotNull @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadline;
    @Size(max=500) private String remark;
}
