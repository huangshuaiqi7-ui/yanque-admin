package cn.yanque.modules.studentexams.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class StudentExamSubmitRes {
    private Long recordId; private Long examId; private String status; private String gradingStatus; private BigDecimal score;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime submitTime;
}
