package cn.yanque.commons.enums;

import lombok.Getter;

/**
 * 业务通用启用状态，适用于用户、角色、课程等业务对象。
 */
@Getter
public enum CommonStatusEnum {

    ACTIVE("启用"),
    INACTIVE("禁用");

    private final String description;

    CommonStatusEnum(String description) {
        this.description = description;
    }

    public static String getDescription(String value) {
        if (value == null) {
            return null;
        }
        for (CommonStatusEnum status : values()) {
            if (status.name().equals(value)) {
                return status.getDescription();
            }
        }
        return null;
    }
}
