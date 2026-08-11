package cn.yanque.modules.students.pojo.entity;

import lombok.Data;
import java.util.Date;

@Data
public class StudentEntity {
    private Long id;
    private String studentNo;
    private String studentName;
    private String studentPhone;
    private String password;
    private String education;
    private Integer gradeYear;
    private String school;
    private String major;
    private String teachingMode;
    private Long classId;
    private String studentTag;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
