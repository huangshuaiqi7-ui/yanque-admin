package cn.yanque.modules.homeworks.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.time.*;
@Data public class HomeworkPrepareRes {
    private Long classId; private String classPeriod;
    @JsonFormat(pattern="yyyy-MM-dd") private LocalDate homeworkDate;
    private String classContent; private String defaultTitle;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime deadline;
}
