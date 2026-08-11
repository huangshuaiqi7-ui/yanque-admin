package cn.yanque.modules.homeworks.mapper;

import cn.yanque.modules.homeworks.pojo.entity.HomeworkEntity;
import cn.yanque.modules.homeworks.pojo.entity.HomeworkSubmissionEntity;
import cn.yanque.modules.homeworks.pojo.vo.resvo.HomeworkRes;
import cn.yanque.modules.homeworks.pojo.vo.resvo.HomeworkSubmissionRes;
import cn.yanque.modules.homeworks.pojo.vo.resvo.StudentHomeworkRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HomeworkMapper {
    List<HomeworkRes> selectPage(@Param("title") String title, @Param("classId") Long classId,
                                 @Param("homeworkDate") LocalDate homeworkDate);
    HomeworkEntity selectById(@Param("id") Long id);
    int countByClassAndDate(@Param("classId") Long classId, @Param("homeworkDate") LocalDate homeworkDate);
    int insert(HomeworkEntity homework);
    int updateAnswer(@Param("id") Long id, @Param("answerObjectKey") String answerObjectKey,
                     @Param("answerFileName") String answerFileName,
                     @Param("answerStudentVisible") Boolean answerStudentVisible);
    List<HomeworkSubmissionRes> selectSubmissions(@Param("homeworkId") Long homeworkId);
    HomeworkSubmissionEntity selectSubmissionById(@Param("id") Long id);
    HomeworkSubmissionEntity selectSubmission(@Param("homeworkId") Long homeworkId,
                                               @Param("studentId") Long studentId);
    int insertSubmission(HomeworkSubmissionEntity submission);
    int updateSubmissionFile(HomeworkSubmissionEntity submission);
    int gradeSubmission(@Param("id") Long id, @Param("score") Integer score,
                        @Param("teacherRemark") String teacherRemark);
    List<StudentHomeworkRes> selectStudentHomeworks(@Param("classId") Long classId,
                                                    @Param("studentId") Long studentId,
                                                    @Param("now") LocalDateTime now);
}
