package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalTaskEntity;
import lombok.Data;

/**
 * Text-to-SQL 评测任务详情。
 */
@Data
public class TextToSqlEvalTaskDetailRes {
    private TextToSqlEvalTaskEntity task;
    private Integer resultCount;
}
