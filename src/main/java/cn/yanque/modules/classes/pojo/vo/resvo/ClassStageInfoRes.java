package cn.yanque.modules.classes.pojo.vo.resvo;

import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ClassStageInfoRes {
    private String stageName;
    private Integer stageNumber;
    private Map<Long, String> freeTeacherName = new LinkedHashMap<>();
}
