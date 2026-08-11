package cn.yanque.modules.classes.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassScheduleDateDetailRes {
    private Long id;
    private Long classId;
    private String classPeriod;
    private Long teacherId;
    private String teacherName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    private Long courseDetailId;
    private String stageName;
    private Integer dayNumber;
    private String courseContent;
    private String classType;
}
