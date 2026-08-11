package cn.yanque.commons.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明访问接口所需的角色或权限。
 * 同一数组内为“或”关系；roles 和 permissions 同时配置时为“且”关系。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {
    String[] roles() default {};
    String[] permissions() default {};
}
