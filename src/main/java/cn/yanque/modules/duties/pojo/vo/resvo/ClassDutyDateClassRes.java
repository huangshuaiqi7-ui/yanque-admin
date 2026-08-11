package cn.yanque.modules.duties.pojo.vo.resvo;

import lombok.Data;

@Data
public class ClassDutyDateClassRes {
    private Long scheduleId;
    private Long classId;
    private String classPeriod;
    private Long campusId;
    private String campusName;
    private String classType;
    private String classTypeDesc;
    private String courseContent;
    private String dutyType;
    private String dutyTypeDesc;
    private String startTime;
    private String endTime;
    private Long teacherId;
    private String teacherName;
}
