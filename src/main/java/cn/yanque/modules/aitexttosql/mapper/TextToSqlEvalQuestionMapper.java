package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalQuestionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlEvalQuestionMapper {
    /**
     * 新增评测样本。
     */
    int insert(TextToSqlEvalQuestionEntity entity);

    /**
     * 更新评测样本。
     */
    int updateById(TextToSqlEvalQuestionEntity entity);

    /**
     * 按 ID 查询评测样本。
     */
    TextToSqlEvalQuestionEntity selectById(@Param("id") Long id);

    /**
     * 按来源运行记录查询样本，用来避免同一运行记录重复加入样本。
     */
    TextToSqlEvalQuestionEntity selectBySourceRunId(@Param("sourceRunId") Long sourceRunId);

    /**
     * 分页筛选评测样本。
     */
    List<TextToSqlEvalQuestionEntity> selectPage(@Param("keyword") String keyword,
                                                 @Param("businessDomain") String businessDomain,
                                                 @Param("evalTarget") String evalTarget,
                                                 @Param("sampleCategory") String sampleCategory,
                                                 @Param("sourceType") String sourceType,
                                                 @Param("status") String status);

    /**
     * 按任务筛选条件查询可执行的 ACTIVE 样本。
     */
    List<TextToSqlEvalQuestionEntity> selectActiveForEval(@Param("businessDomain") String businessDomain,
                                                          @Param("evalTarget") String evalTarget,
                                                          @Param("sampleCategory") String sampleCategory);

    /**
     * 查询用户勾选的 ACTIVE 样本。
     */
    List<TextToSqlEvalQuestionEntity> selectActiveByIds(@Param("ids") List<Long> ids);
}
