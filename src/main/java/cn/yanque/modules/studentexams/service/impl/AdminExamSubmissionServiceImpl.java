package cn.yanque.modules.studentexams.service.impl;
import cn.yanque.commons.apires.*; import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.examquestions.mapper.ExamQuestionMapper; import cn.yanque.modules.exams.mapper.ExamMapper;
import cn.yanque.modules.studentexams.mapper.StudentExamMapper; import cn.yanque.modules.studentexams.pojo.entity.*;
import cn.yanque.modules.studentexams.pojo.vo.reqvo.ExamGradeReq; import cn.yanque.modules.studentexams.pojo.vo.resvo.*;
import cn.yanque.modules.studentexams.service.AdminExamSubmissionService;
import com.github.pagehelper.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.util.*;
import java.time.LocalDateTime;
@Service public class AdminExamSubmissionServiceImpl implements AdminExamSubmissionService {
    private static final Set<String> OBJECTIVE=Set.of("SINGLE","MULTIPLE","JUDGE");
    private final StudentExamMapper mapper;private final ExamMapper examMapper;private final ExamQuestionMapper questionMapper;
    public AdminExamSubmissionServiceImpl(StudentExamMapper mapper,ExamMapper examMapper,ExamQuestionMapper questionMapper){this.mapper=mapper;this.examMapper=examMapper;this.questionMapper=questionMapper;}
    public PageResult<ExamSubmissionListRes> page(Long examId,int pageNum,int pageSize){
        if(examMapper.selectById(examId)==null)throw BusinessException.of(CommonErrorCode.EXAM_NOT_FOUND);
        mapper.markExpiredRecordsByExam(examId,LocalDateTime.now());
        PageHelper.startPage(pageNum,pageSize);List<ExamSubmissionListRes> rows=mapper.selectExamSubmissions(examId);PageInfo<ExamSubmissionListRes> info=new PageInfo<>(rows);
        rows.forEach(x->x.setRecordStatusText(Map.of("NOT_STARTED","未开始","IN_PROGRESS","进行中","SUBMITTED","已提交","TIMEOUT","已超时").getOrDefault(x.getRecordStatus(),x.getRecordStatus())));
        return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows);
    }
    public ExamSubmissionDetailRes detail(Long recordId){StudentExamRecordEntity record=requireSubmitted(recordId);return buildDetail(record.getId());}
    @Transactional public ExamSubmissionDetailRes grade(Long recordId,ExamGradeReq req){
        requireSubmitted(recordId);Set<Long> ids=new HashSet<>();
        for(ExamGradeReq.AnswerGrade grade:Optional.ofNullable(req.getAnswers()).orElse(List.of())){
            if(!ids.add(grade.getAnswerId()))throw BusinessException.of(CommonErrorCode.EXAM_GRADE_ANSWER_INVALID);
            StudentExamAnswerEntity answer=mapper.selectAnswerById(grade.getAnswerId());
            if(answer==null||!recordId.equals(answer.getRecordId()))throw BusinessException.of(CommonErrorCode.EXAM_GRADE_ANSWER_INVALID);
            if(OBJECTIVE.contains(answer.getQuestionType()))throw BusinessException.of(CommonErrorCode.EXAM_GRADE_OBJECTIVE_FORBIDDEN);
            if(grade.getScore().compareTo(BigDecimal.ZERO)<0||grade.getScore().compareTo(answer.getQuestionScore())>0)throw BusinessException.of(CommonErrorCode.EXAM_GRADE_SCORE_INVALID);
            mapper.updateAnswerScore(answer.getId(),grade.getScore());
        }
        int unscored=mapper.countUnscoredAnswers(recordId);String status=unscored==0?"COMPLETED":"GRADING";BigDecimal total=mapper.sumAnswerScores(recordId);
        mapper.updateRecordGrade(recordId,status,total);return buildDetail(recordId);
    }
    private ExamSubmissionDetailRes buildDetail(Long id){ExamSubmissionDetailRes res=mapper.selectRecordDetail(id);if(res==null)throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_RECORD_NOT_FOUND);List<ExamSubmissionDetailRes.Question> qs=mapper.selectSubmissionQuestions(id);qs.forEach(q->q.setOptions(questionMapper.selectOptions(q.getQuestionId()).stream().map(x->{StudentExamPaperRes.Option o=new StudentExamPaperRes.Option();o.setId(x.getId());o.setQuestionId(x.getQuestionId());o.setOptionKey(x.getOptionKey());o.setOptionContent(x.getOptionContent());return o;}).toList()));res.setQuestions(qs);return res;}
    private StudentExamRecordEntity requireSubmitted(Long id){StudentExamRecordEntity r=mapper.selectRecord(id);if(r==null)throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_RECORD_NOT_FOUND);if(!"SUBMITTED".equals(r.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);return r;}
}
