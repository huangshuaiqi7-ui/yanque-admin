package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建 Text-to-SQL 评测任务。
 *
 * 创建后会立即交给后台线程异步执行。
 * 如果 evalQuestionIds 不为空，就只跑前端勾选的样本；
 * 如果 evalQuestionIds 为空，就按 businessDomain、evalTarget、sampleCategory 筛选 ACTIVE 样本。
 */
@Data
public class TextToSqlEvalTaskCreateReq {
    /**
     * 任务名称。
     *
     * 只用于页面展示和后续排查，例如：
     * Text-to-SQL评测 2026/9/2 15:30
     */
    @NotBlank(message = "请输入任务名称")
    @Size(max = 128, message = "任务名称不能超过128个字符")
    private String name;

    /**
     * 业务环境筛选。
     *
     * 例如 order、student、teaching。
     * 当 evalQuestionIds 为空时生效，用来筛选要跑的 ACTIVE 样本。
     */
    private String businessDomain;

    /**
     * 评测目标筛选。
     *
     * 例如 END_TO_END、TABLE_SELECTION、SQL_GENERATION。
     * 当 evalQuestionIds 为空时生效。
     */
    private String evalTarget;

    /**
     * 样本场景筛选。
     *
     * 例如 NORMAL、REGRESSION、AMBIGUOUS。
     * 当 evalQuestionIds 为空时生效。
     */
    private String sampleCategory;

    /**
     * 前端勾选的样本 ID 列表。
     *
     * 这个字段优先级最高。
     * 只要传了 ID，后端就只跑这些样本，并且只会保留状态为 ACTIVE 的样本。
     */
    private List<Long> evalQuestionIds;
}
