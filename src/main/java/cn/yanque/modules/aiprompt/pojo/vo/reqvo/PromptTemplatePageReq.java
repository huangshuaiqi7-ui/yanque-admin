package cn.yanque.modules.aiprompt.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PromptTemplatePageReq {
    /** 当前页码，从 1 开始。 */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    /** 每页条数。 */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 1000, message = "每页条数不能超过1000")
    private Integer pageSize = 10;

    /** 关键词，支持按提示词名称或编码模糊查询。 */
    private String keyword;

    /** 所属 Agent 编码，精确匹配。 */
    private String agentCode;

    /** 启用状态，可选值为 ACTIVE 或 INACTIVE。 */
    private String status;
}
