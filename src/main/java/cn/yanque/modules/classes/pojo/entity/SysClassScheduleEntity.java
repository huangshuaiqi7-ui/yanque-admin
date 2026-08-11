package cn.yanque.modules.classes.pojo.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Data
public class SysClassScheduleEntity {
    private Long id;
    private Long classId;
    private Long teacherId;
    private LocalDate scheduleDate;
    private Long courseDetailId;
    private String courseContent;
    private String classType;
    private Date createdAt;
    private Date updatedAt;
}
