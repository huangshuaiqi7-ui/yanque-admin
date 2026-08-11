package cn.yanque.modules.classes.pojo.vo.resvo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TeacherScheduleRowRes {
    private Long teacherId;
    private String teacherName;
    private LocalDate scheduleDate;
    private Long classId;
    private String classPeriod;
}
