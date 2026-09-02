package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建 Text-to-SQL 评测任务。
 */
@Data
public class TextToSqlEvalTaskCreateReq {
    @NotBlank(message = "请输入任务名称")
    @Size(max = 128, message = "任务名称不能超过128个字符")
    private String name;

    private String businessDomain;
    private String evalTarget;
    private String sampleCategory;
    private List<Long> evalQuestionIds;
}
