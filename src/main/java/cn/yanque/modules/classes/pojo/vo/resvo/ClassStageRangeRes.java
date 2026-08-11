package cn.yanque.modules.classes.pojo.vo.resvo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassStageRangeRes {
    private String stageName;
    private Integer stageNumber;
    private LocalDate startDate;
    private LocalDate endDate;
}
