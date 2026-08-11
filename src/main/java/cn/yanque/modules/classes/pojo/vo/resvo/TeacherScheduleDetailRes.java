package cn.yanque.modules.classes.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TeacherScheduleDetailRes {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate teacheringDate;
    private Long classId;
    private String classPeriod;
}
