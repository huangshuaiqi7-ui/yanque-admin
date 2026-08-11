package cn.yanque.modules.users.pojo.vo.resvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户详情响应对象。
 */
@Data
@Schema(description = "用户详情响应")
public class UserDetailRes {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "飞书 union_id")
    private String unionId;

    @Schema(description = "状态", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    /** 状态中文描述，由 status 自动转换
     *   校验, 只让statusDesc,状态的描述, 只能是启动,禁用两种状态.
     *
     *   自定义注解, 让这个状态只允许是启动,禁用两种状态.
     * */

    private String statusDesc;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;

    /** 当前用户已分配的角色ID。 */
    private List<Long> roleIds;

    /**
     * 设置状态时同步生成状态描述。
     *
     * @param status 用户状态
     */
   /* public void setStatus(String status) {
        this.status = status;
        this.setStatusDesc(ActiveEnum.valueOf(status).getDesc());
    }*/
}
