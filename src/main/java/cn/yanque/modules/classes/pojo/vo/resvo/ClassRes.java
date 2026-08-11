package cn.yanque.modules.classes.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class ClassRes {
    private Long id;
    private String classPeriod;
    private Long headTeacherId;
    private String headTeacherName;
    private Long campusId;
    private String campusName;
    private Long courseId;
    private String courseName;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updatedAt;
}
