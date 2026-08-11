package cn.yanque.modules.classes.pojo.vo.resvo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherScheduleItemRes {
    private Long teacherId;
    private String teacherName;
    private List<TeacherScheduleDetailRes> teacherDetailList = new ArrayList<>();
}
