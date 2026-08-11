package cn.yanque.commons.enums;

import lombok.Getter;

@Getter
public enum ClassDutyTypeEnum {
    EVENING_STUDY_CLASS("晚自习班级值班", "19:00", "21:00"),
    EVENING_STUDY_CAMPUS("晚自习校区统一值班", "21:00", "22:30"),
    SELF_STUDY_CLASS("自习日班级值班", "09:00", "18:00");

    private final String description;
    private final String startTime;
    private final String endTime;

    ClassDutyTypeEnum(String description, String startTime, String endTime) {
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
