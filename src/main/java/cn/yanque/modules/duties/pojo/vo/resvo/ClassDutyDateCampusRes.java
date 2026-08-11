package cn.yanque.modules.duties.pojo.vo.resvo;

import lombok.Data;

@Data
public class ClassDutyDateCampusRes {
    private Long campusId;
    private String campusName;
    private String dutyType;
    private String dutyTypeDesc;
    private String startTime;
    private String endTime;
    private Long teacherId;
    private String teacherName;
}
