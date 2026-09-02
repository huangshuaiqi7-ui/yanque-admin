package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlEvalAssertionResultMapper {
    /**
     * 批量新增断言执行结果。
     */
    int insertBatch(@Param("list") List<TextToSqlEvalAssertionResultEntity> list);

    /**
     * 查询某条样本执行结果下的全部断言结果。
     */
    List<TextToSqlEvalAssertionResultEntity> selectByEvalResultId(@Param("evalResultId") Long evalResultId);

    /**
     * 删除某条样本执行结果下的全部断言结果。
     */
    int deleteByEvalResultId(@Param("evalResultId") Long evalResultId);
}
