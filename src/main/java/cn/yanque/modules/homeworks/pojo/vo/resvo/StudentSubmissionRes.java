package cn.yanque.modules.homeworks.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.time.LocalDateTime;
@Data public class StudentSubmissionRes {
    private Long id; private Long homeworkId; private String contentFileName;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime submitTime; private Boolean lateSubmitted;
}
