package cn.yanque.modules.homeworks.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkSubmissionEntity {
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private Long classId;
    private String contentObjectKey;
    private String contentFileName;
    private LocalDateTime submitTime;
    private Boolean lateSubmitted;
    private String teacherRemark;
    private Integer score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
