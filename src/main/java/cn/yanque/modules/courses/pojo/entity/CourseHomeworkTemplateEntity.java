package cn.yanque.modules.courses.pojo.entity;

import lombok.Data;
import java.util.Date;

@Data
public class CourseHomeworkTemplateEntity {
    private Long id;
    private Long courseId;
    private String teachingMode;
    private String stageName;
    private Integer dayNumber;
    private String contentObjectKey;
    private String contentFileName;
    private String status;
    private String remark;
    private Date createdAt;
    private Date updatedAt;
}
