package cn.yanque.modules.homeworks.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkSubmissionRes {
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private String studentPhone;
    private String contentObjectKey;
    private String contentFileName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    private Boolean lateSubmitted;
    private String teacherRemark;
    private Integer score;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
