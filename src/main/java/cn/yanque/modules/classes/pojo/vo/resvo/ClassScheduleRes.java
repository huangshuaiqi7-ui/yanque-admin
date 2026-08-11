package cn.yanque.modules.classes.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassScheduleRes {
    private Long id;
    private Long classId;
    private Long teacherId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    private Long courseDetailId;
    private String courseContent;
    private String classType;
}
