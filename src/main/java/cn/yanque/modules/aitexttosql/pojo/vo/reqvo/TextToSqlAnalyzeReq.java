package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 前端数据分析页提交的自然语言查询。
 */
@Data
public class TextToSqlAnalyzeReq {
    @NotBlank(message = "请输入要分析的问题")
    private String question;

    @Min(value = 1, message = "最大返回行数不能小于1")
    @Max(value = 500, message = "最大返回行数不能超过500")
    private Integer maxRows = 100;

    /**
     * Python LangGraph interrupt 暂停后返回的会话 ID。
     * 用户回答澄清问题时，前端需要把这个 ID 原样传回，Python 才能从 checkpoint 继续执行。
     */
    private String conversationId;

    /**
     * 用户对澄清问题的回答。
     * 为空时表示发起一次新的自然语言分析；不为空时表示恢复之前暂停的图。
     */
    private String clarificationAnswer;
}
