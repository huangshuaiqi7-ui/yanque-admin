package cn.yanque.modules.courses.pojo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysCourseEntity {
    private Long id;
    private String courseName;
    private Integer courseDays;
    private String teachingMode;
    private String materialPath;
    private Date createdAt;
    private Date updatedAt;
}
