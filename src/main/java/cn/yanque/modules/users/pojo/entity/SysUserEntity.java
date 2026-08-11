package cn.yanque.modules.users.pojo.entity;

import lombok.Data;

import java.util.Date;

/**
 * 系统用户实体，对应 sys_user 表。
 */
@Data
public class SysUserEntity {
    /** 用户ID */
    private Long id;
    /** 登录用户名 */
    private String username;
    /** 登录密码，当前为测试阶段明文存储 */
    private String password;
    /** 展示昵称 */
    private String nickname;
    /** 真实姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 飞书 union_id */
    private String unionId;
    /** 状态，ACTIVE启用，INACTIVE禁用 */
    private String status;
    /** 创建时间 */
    private Date createdAt;
    /** 更新时间 */
    private Date updatedAt;
}