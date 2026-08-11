package cn.yanque.modules.studentexams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.time.LocalDateTime;
@Data public class StudentExamStartRes {
    private Long recordId; private Long examId; private Long paperId; private String status;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadlineTime;
}
