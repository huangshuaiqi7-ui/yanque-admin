package cn.yanque.modules.exampapers.mapper;
import cn.yanque.modules.exampapers.pojo.entity.*;
import cn.yanque.modules.exampapers.pojo.vo.resvo.ExamPaperRes;
import org.apache.ibatis.annotations.*; import java.util.*;
@Mapper public interface ExamPaperMapper {
    List<ExamPaperRes> selectPage(@Param("courseId") Long courseId,@Param("stageName") String stageName,@Param("paperName") String paperName);
    ExamPaperEntity selectById(@Param("id") Long id);
    ExamPaperRes selectDetail(@Param("id") Long id);
    List<ExamPaperRes.QuestionRes> selectQuestions(@Param("paperId") Long paperId);
    int insert(ExamPaperEntity paper);
    int batchInsertQuestions(@Param("items") List<ExamPaperQuestionEntity> items);
    int deleteQuestions(@Param("paperId") Long paperId);
    int deleteById(@Param("id") Long id);
}
