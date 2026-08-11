package cn.yanque.modules.examquestions.mapper;
import cn.yanque.modules.examquestions.pojo.entity.*;
import cn.yanque.modules.examquestions.pojo.vo.resvo.*;
import org.apache.ibatis.annotations.*; import java.util.*;
@Mapper public interface ExamQuestionMapper {
    List<ExamQuestionRes> selectPage(@Param("questionType") String questionType,@Param("courseId") Long courseId,@Param("stageName") String stageName,
        @Param("difficulty") String difficulty,@Param("status") String status,@Param("keyword") String keyword);
    ExamQuestionEntity selectById(@Param("id") Long id);
    List<ExamQuestionRes.OptionRes> selectOptions(@Param("questionId") Long questionId);
    List<ExamQuestionCourseStageRes> selectCourseStages(@Param("questionId") Long questionId);
    int insert(ExamQuestionEntity question);
    int updateById(ExamQuestionEntity question);
    int updateStatus(@Param("id") Long id,@Param("status") String status);
    int deleteById(@Param("id") Long id);
    int deleteOptions(@Param("questionId") Long questionId);
    int deleteCourseStages(@Param("questionId") Long questionId);
    int batchInsertOptions(@Param("items") List<ExamQuestionOptionEntity> items);
    int batchInsertCourseStages(@Param("questionId") Long questionId,@Param("items") List<ExamQuestionCourseStageRes> items);
    int countQuestionScope(@Param("questionId") Long questionId,@Param("courseId") Long courseId,@Param("stageName") String stageName);
}
