package cn.yanque.modules.classes.pojo.config;

import lombok.Data;
import java.util.List;

@Data
public class TeachingScheduleRule {
    private List<Integer> classDays;
    private List<Integer> selfStudyDays;
    private List<Integer> restDays;
    private Boolean holidayRest;
    /** 法定节假日，格式：yyyy-MM-dd。 */
    private List<String> holidays;
}
