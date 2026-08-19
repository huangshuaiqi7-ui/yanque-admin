package cn.yanque.modules.aiprompt.pojo.vo.resvo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromptTemplateVersionRes {
    /** 提示词版本ID。 */
    private Long id;

    /** 所属提示词模板ID。 */
    private Long templateId;

    /** 模板内递增版本号。 */
    private Integer versionNo;

    /** 提示词内容。 */
    private String content;

    /** 变量说明 JSON 字符串。 */
    private String variables;

    /** 本次修改说明。 */
    private String changeNote;

    /** 创建人用户ID。 */
    private Long createBy;

    /** 创建人名称，本期无稳定来源时为空。 */
    private String createByName;

    /** 是否为当前启用版本。 */
    private Boolean current;

    /** 版本状态，CURRENT当前，UNPUBLISHED未发布，HISTORY历史。 */
    private String status;

    /** 版本状态中文文案。 */
    private String statusText;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 最后更新时间。 */
    private LocalDateTime updateTime;
}
