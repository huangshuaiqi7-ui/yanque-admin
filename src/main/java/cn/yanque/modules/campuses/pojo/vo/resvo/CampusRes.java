package cn.yanque.modules.campuses.pojo.vo.resvo;

import lombok.Data;

import java.util.Date;

@Data
public class CampusRes {
    private Long id;
    private String campusLocation;
    private String managerName;
    private String managerPhone;
    private Date createdAt;
    private Date updatedAt;
}
