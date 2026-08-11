package cn.yanque.modules.courses.pojo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysCourseDetailEntity {
    private Long id;
    private Long courseId;
    private String stageName;
    private Integer dayNumber;
    private String classContent;
    private Date createdAt;
    private Date updatedAt;
}
