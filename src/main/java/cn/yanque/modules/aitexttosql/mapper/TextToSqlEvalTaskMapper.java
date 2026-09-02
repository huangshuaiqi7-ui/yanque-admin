package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TextToSqlEvalTaskMapper {
    /**
     * 新增评测任务。
     */
    int insert(TextToSqlEvalTaskEntity entity);

    /**
     * 标记任务开始运行，并写入本次样本总数。
     */
    int updateRunning(@Param("id") Long id, @Param("totalCount") int totalCount);

    /**
     * 标记任务结束，并写入汇总统计。
     */
    int updateFinished(@Param("id") Long id,
                       @Param("status") String status,
                       @Param("passCount") int passCount,
                       @Param("failCount") int failCount,
                       @Param("interruptedCount") int interruptedCount,
                       @Param("passRate") BigDecimal passRate,
                       @Param("durationMs") Long durationMs,
                       @Param("errorMessage") String errorMessage);

    /**
     * 重新写入任务汇总统计。
     */
    int updateCounts(@Param("id") Long id,
                     @Param("passCount") int passCount,
                     @Param("failCount") int failCount,
                     @Param("interruptedCount") int interruptedCount,
                     @Param("passRate") BigDecimal passRate);

    /**
     * 按 ID 查询评测任务。
     */
    TextToSqlEvalTaskEntity selectById(@Param("id") Long id);

    /**
     * 分页筛选评测任务。
     */
    List<TextToSqlEvalTaskEntity> selectPage(@Param("keyword") String keyword,
                                             @Param("businessDomain") String businessDomain,
                                             @Param("evalTarget") String evalTarget,
                                             @Param("sampleCategory") String sampleCategory,
                                             @Param("status") String status);
}
