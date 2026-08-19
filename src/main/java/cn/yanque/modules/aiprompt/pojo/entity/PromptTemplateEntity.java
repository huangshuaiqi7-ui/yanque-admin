package cn.yanque.modules.aiprompt.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromptTemplateEntity {
    private Long id;
    /** 稳定业务编码，创建后不允许修改，后续用于运行时读取提示词。 */
    private String code;
    private String name;
    private String agentCode;
    private String promptType;
    private String sceneCode;
    private String status;
    /** 版本管理上线后指向当前发布版本；本期只预留和展示。 */
    private Long activeVersionId;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
