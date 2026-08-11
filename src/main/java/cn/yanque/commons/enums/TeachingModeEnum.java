package cn.yanque.commons.enums;

import lombok.Getter;

@Getter
public enum TeachingModeEnum {
    ONLINE("线上课程"),
    OFFLINE("线下课程");

    private final String description;

    TeachingModeEnum(String description) {
        this.description = description;
    }
}
