package cn.yanque.modules.studentexams.mapper;
import cn.yanque.modules.studentexams.pojo.entity.*; import cn.yanque.modules.studentexams.pojo.vo.resvo.*;
import org.apache.ibatis.annotations.*; import java.math.BigDecimal; import java.util.*;
@Mapper public interface StudentExamMapper {
    List<StudentExamRes> selectStudentExams(@Param("studentId") Long studentId,@Param("classId") Long classId);
    StudentExamRecordEntity selectRecord(@Param("id") Long id);
    StudentExamRecordEntity selectRecordByExamStudent(@Param("examId") Long examId,@Param("studentId") Long studentId);
    int insertRecord(StudentExamRecordEntity record);
    int markTimeout(@Param("id") Long id);
    int submitRecord(@Param("id") Long id,@Param("submitTime") java.time.LocalDateTime submitTime,@Param("gradingStatus") String gradingStatus,@Param("score") BigDecimal score);
    List<ExamPaperQuestionRow> selectPaperQuestions(@Param("paperId") Long paperId);
    int batchInsertAnswers(@Param("items") List<StudentExamAnswerEntity> items);
    List<ExamSubmissionListRes> selectExamSubmissions(@Param("examId") Long examId);
    ExamSubmissionDetailRes selectRecordDetail(@Param("recordId") Long recordId);
    List<ExamSubmissionDetailRes.Question> selectSubmissionQuestions(@Param("recordId") Long recordId);
    StudentExamAnswerEntity selectAnswerById(@Param("id") Long id);
    int updateAnswerScore(@Param("id") Long id,@Param("score") BigDecimal score);
    int countUnscoredAnswers(@Param("recordId") Long recordId);
    BigDecimal sumAnswerScores(@Param("recordId") Long recordId);
    int updateRecordGrade(@Param("id") Long id,@Param("gradingStatus") String gradingStatus,@Param("score") BigDecimal score);
    int markExpiredRecordsByExam(@Param("examId") Long examId,@Param("now") java.time.LocalDateTime now);
}
