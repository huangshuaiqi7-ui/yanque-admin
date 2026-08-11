package cn.yanque.modules.students.pojo.vo.resvo;

import lombok.Data;
import java.util.Date;

@Data
public class StudentRes {
    private Long id;
    private String studentNo;
    private String studentName;
    private String studentPhone;
    private String education;
    private Integer gradeYear;
    private String school;
    private String major;
    private String teachingMode;
    private Long classId;
    private String classPeriod;
    private String productContent;
    private String studentTag;
    private String status;
    private Boolean sopAssigned;
    private Long sopId;
    private Long sopMentorId;
    private String sopMentorName;
    private String sopVideoObjectKey;
    private String sopVideoFileName;
    private Date sopTime;
    private Date createdAt;
    private Date updatedAt;
}
