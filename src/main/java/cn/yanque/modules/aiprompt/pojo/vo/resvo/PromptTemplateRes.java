package cn.yanque.modules.aiprompt.pojo.vo.resvo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromptTemplateRes {
    /** 提示词模板ID。 */
    private Long id;

    /** 提示词编码，稳定业务标识。 */
    private String code;

    /** 提示词名称。 */
    private String name;

    /** 所属 Agent 编码。 */
    private String agentCode;

    /** 提示词类型，SYSTEM 或 USER。 */
    private String promptType;

    /** 使用场景，例如 CHAT、RAG、SUMMARY、JUDGE、STRUCTURED_EXTRACT。 */
    private String sceneCode;

    /** 启用状态，ACTIVE 或 INACTIVE。 */
    private String status;

    /** 启用状态中文文案。 */
    private String statusText;

    /** 当前启用版本ID，本期仅预留展示。 */
    private Long activeVersionId;

    /** 提示词模板说明。 */
    private String description;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
