package cn.yanque.modules.duties.pojo.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SysClassDutyEntity {
    private Long id;
    private Long classId;
    private Long campusId;
    private Long teacherId;
    private LocalDate dutyDate;
    private String dutyType;
    private String startTime;
    private String endTime;
    private String remark;
}
