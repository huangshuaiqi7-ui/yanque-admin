package cn.yanque.modules.courses.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class CourseHomeworkTemplateRes {
    private Long id;
    private Long courseId;
    private String teachingMode;
    private String stageName;
    private Integer dayNumber;
    private String contentObjectKey;
    private String contentFileName;
    private String status;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updatedAt;
}
