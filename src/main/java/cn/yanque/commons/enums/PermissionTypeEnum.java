package cn.yanque.commons.enums;

import lombok.Getter;

@Getter
public enum PermissionTypeEnum {
    API("接口"),
    MENU("菜单"),
    BUTTON("按钮");

    private final String description;

    PermissionTypeEnum(String description) {
        this.description = description;
    }
}
