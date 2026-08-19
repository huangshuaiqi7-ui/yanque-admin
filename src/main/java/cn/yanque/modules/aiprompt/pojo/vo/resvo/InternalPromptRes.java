package cn.yanque.modules.aiprompt.pojo.vo.resvo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内部服务读取当前启用提示词的响应。
 */
@Data
public class InternalPromptRes {
    /** 提示词编码。 */
    private String code;
    /** 提示词名称。 */
    private String name;
    /** 所属 Agent 编码。 */
    private String agentCode;
    /** 提示词类型，SYSTEM 或 USER。 */
    private String promptType;
    /** 使用场景编码。 */
    private String sceneCode;
    /** 当前启用版本ID。 */
    private Long versionId;
    /** 当前启用版本号。 */
    private Integer versionNo;
    /** 当前启用版本提示词内容。 */
    private String content;
    /** 变量说明 JSON 字符串。 */
    private String variables;
    /** 当前启用版本最后更新时间。 */
    private LocalDateTime updateTime;
}
