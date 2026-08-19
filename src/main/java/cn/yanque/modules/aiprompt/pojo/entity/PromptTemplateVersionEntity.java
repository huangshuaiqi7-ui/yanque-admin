package cn.yanque.modules.aiprompt.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromptTemplateVersionEntity {
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
    /** 创建人用户ID，本期允许为空。 */
    private Long createBy;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 最后更新时间。 */
    private LocalDateTime updateTime;
}
