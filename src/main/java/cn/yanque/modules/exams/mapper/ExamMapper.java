package cn.yanque.modules.exams.mapper;
import cn.yanque.modules.exams.pojo.entity.ExamEntity; import cn.yanque.modules.exams.pojo.vo.resvo.ExamRes;
import org.apache.ibatis.annotations.*; import java.time.LocalDateTime; import java.util.List;
@Mapper public interface ExamMapper {
    List<ExamRes> selectPage(@Param("paperId") Long paperId,@Param("classId") Long classId,@Param("invigilatorUserId") Long invigilatorUserId);
    ExamEntity selectById(@Param("id") Long id); ExamRes selectDetail(@Param("id") Long id);
    int countClassTimeConflict(@Param("classId") Long classId,@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("excludeId") Long excludeId);
    int insert(ExamEntity exam); int updateById(ExamEntity exam);
    int updateAnswerVisible(@Param("id") Long id,@Param("answerVisible") Boolean answerVisible);
    int deleteById(@Param("id") Long id);
}
