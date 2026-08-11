package cn.yanque.modules.homeworks.pojo.vo.reqvo;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
@Data public class HomeworkPageReq {
    private String title; private Long classId;
    @DateTimeFormat(pattern="yyyy-MM-dd") private LocalDate homeworkDate;
    @Min(1) private Integer pageNum=1;
    @Min(1) @Max(1000) private Integer pageSize=10;
}
