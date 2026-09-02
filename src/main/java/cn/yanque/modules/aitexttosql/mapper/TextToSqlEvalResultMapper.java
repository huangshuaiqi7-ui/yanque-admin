package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlEvalResultMapper {
    /**
     * 新增单条样本执行结果。
     */
    int insert(TextToSqlEvalResultEntity entity);

    /**
     * 更新单条样本执行结果。
     */
    int updateById(TextToSqlEvalResultEntity entity);

    /**
     * 按 ID 查询样本执行结果。
     */
    TextToSqlEvalResultEntity selectById(@Param("id") Long id);

    /**
     * 查询某个评测任务下的样本结果。
     */
    List<TextToSqlEvalResultEntity> selectByTaskId(@Param("evalTaskId") Long evalTaskId,
                                                   @Param("passed") Boolean passed,
                                                   @Param("resultStatus") String resultStatus);

    /**
     * 统计某个评测任务下已经落库的结果数量。
     */
    int countByTaskId(@Param("evalTaskId") Long evalTaskId);
}
