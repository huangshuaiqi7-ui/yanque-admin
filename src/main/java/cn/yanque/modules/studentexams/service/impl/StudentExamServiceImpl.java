package cn.yanque.modules.studentexams.service.impl;
import cn.hutool.core.util.StrUtil; import cn.yanque.commons.apires.*; import cn.yanque.commons.context.UserContext; import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.examquestions.mapper.ExamQuestionMapper; import cn.yanque.modules.exams.mapper.ExamMapper; import cn.yanque.modules.exams.pojo.entity.ExamEntity;
import cn.yanque.modules.exampapers.mapper.ExamPaperMapper; import cn.yanque.modules.exampapers.pojo.vo.resvo.ExamPaperRes;
import cn.yanque.modules.students.mapper.StudentMapper; import cn.yanque.modules.students.pojo.entity.StudentEntity;
import cn.yanque.modules.studentexams.mapper.StudentExamMapper; import cn.yanque.modules.studentexams.pojo.entity.*;
import cn.yanque.modules.studentexams.pojo.vo.reqvo.StudentExamSubmitReq; import cn.yanque.modules.studentexams.pojo.vo.resvo.*; import cn.yanque.modules.studentexams.service.StudentExamService;
import com.github.pagehelper.*; import org.springframework.dao.DuplicateKeyException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.*; import java.util.function.Function; import java.util.stream.Collectors;
@Service public class StudentExamServiceImpl implements StudentExamService {
    private static final Set<String> OBJECTIVE=Set.of("SINGLE","MULTIPLE","JUDGE");
    private final StudentExamMapper mapper;private final StudentMapper studentMapper;private final ExamMapper examMapper;private final ExamPaperMapper paperMapper;private final ExamQuestionMapper questionMapper;
    public StudentExamServiceImpl(StudentExamMapper mapper,StudentMapper studentMapper,ExamMapper examMapper,ExamPaperMapper paperMapper,ExamQuestionMapper questionMapper){this.mapper=mapper;this.studentMapper=studentMapper;this.examMapper=examMapper;this.paperMapper=paperMapper;this.questionMapper=questionMapper;}
    public PageResult<StudentExamRes> myExams(int pageNum,int pageSize){
        StudentEntity student=currentStudent();requireClass(student);PageHelper.startPage(pageNum,pageSize);List<StudentExamRes> rows=mapper.selectStudentExams(student.getId(),student.getClassId());PageInfo<StudentExamRes> info=new PageInfo<>(rows);
        LocalDateTime now=LocalDateTime.now();rows.forEach(x->applyStatus(x,now));return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows);
    }
    public StudentExamStartRes start(Long examId){
        StudentEntity student=currentStudent();requireClass(student);ExamEntity exam=requireExam(examId);if(!student.getClassId().equals(exam.getClassId()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_FORBIDDEN);
        StudentExamRecordEntity old=mapper.selectRecordByExamStudent(examId,student.getId());
        if(old!=null){if("IN_PROGRESS".equals(old.getStatus())&&LocalDateTime.now().isBefore(old.getDeadlineTime()))return startRes(old,exam.getPaperId());if("SUBMITTED".equals(old.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_TIMEOUT);}
        LocalDateTime now=LocalDateTime.now();if(now.isBefore(exam.getStartTime())||now.isAfter(exam.getEndTime()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_NOT_AVAILABLE);
        StudentExamRecordEntity record=new StudentExamRecordEntity();record.setExamId(examId);record.setStudentId(student.getId());record.setStartTime(now);record.setDeadlineTime(now.plusMinutes(exam.getDurationMinutes()));record.setStatus("IN_PROGRESS");record.setGradingStatus("PENDING");
        try{if(mapper.insertRecord(record)!=1)throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_OPERATION_FAILED);}catch(DuplicateKeyException e){record=mapper.selectRecordByExamStudent(examId,student.getId());}
        return startRes(record,exam.getPaperId());
    }
    public StudentExamPaperRes paper(Long recordId){
        StudentEntity student=currentStudent();StudentExamRecordEntity record=ownedRecord(recordId,student.getId());ensureInProgress(record);
        ExamSubmissionDetailRes meta=mapper.selectRecordDetail(recordId);List<ExamPaperQuestionRow> rows=mapper.selectPaperQuestions(meta.getPaperId());
        StudentExamPaperRes res=new StudentExamPaperRes();res.setRecordId(recordId);res.setExamId(record.getExamId());res.setPaperId(meta.getPaperId());res.setPaperName(meta.getPaperName());res.setClassPeriod(meta.getClassPeriod());res.setCourseId(paperMapper.selectById(meta.getPaperId()).getCourseId());res.setStageName(meta.getStageName());res.setTotalScore(meta.getTotalScore());res.setStartTime(record.getStartTime());res.setDeadlineTime(record.getDeadlineTime());res.setRecordStatus(record.getStatus());res.setGradingStatus(record.getGradingStatus());
        ExamEntity exam=requireExam(record.getExamId());res.setClassId(exam.getClassId());res.setQuestions(rows.stream().map(this::toPaperQuestion).toList());return res;
    }
    @Transactional public StudentExamSubmitRes submit(Long recordId,StudentExamSubmitReq req){
        StudentEntity student=currentStudent();StudentExamRecordEntity record=ownedRecord(recordId,student.getId());
        if(!"IN_PROGRESS".equals(record.getStatus())){if("SUBMITTED".equals(record.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_TIMEOUT);}
        LocalDateTime now=LocalDateTime.now();if(!now.isBefore(record.getDeadlineTime()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_TIMEOUT);
        ExamEntity exam=requireExam(record.getExamId());List<ExamPaperQuestionRow> questions=mapper.selectPaperQuestions(exam.getPaperId());
        Map<Long,String> submitted=new HashMap<>();for(StudentExamSubmitReq.Answer x:Optional.ofNullable(req.getAnswers()).orElse(List.of()))if(submitted.put(x.getQuestionId(),x.getAnswerContent())!=null)throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ANSWER_INVALID);
        Set<Long> validIds=questions.stream().map(ExamPaperQuestionRow::getQuestionId).collect(Collectors.toSet());if(!validIds.containsAll(submitted.keySet()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ANSWER_INVALID);
        boolean hasSubjective=false;BigDecimal total=BigDecimal.ZERO;List<StudentExamAnswerEntity> answers=new ArrayList<>();
        for(ExamPaperQuestionRow q:questions){String content=StrUtil.trim(submitted.get(q.getQuestionId()));StudentExamAnswerEntity a=new StudentExamAnswerEntity();a.setRecordId(recordId);a.setExamId(record.getExamId());a.setPaperId(exam.getPaperId());a.setQuestionId(q.getQuestionId());a.setQuestionType(q.getQuestionType());a.setQuestionScore(q.getQuestionScore());a.setAnswerContent(content);
            if(OBJECTIVE.contains(q.getQuestionType())){boolean correct=normalizeAnswer(q.getQuestionType(),content).equals(normalizeAnswer(q.getQuestionType(),q.getCorrectAnswer()));a.setCorrect(correct);a.setScore(correct?q.getQuestionScore():BigDecimal.ZERO);total=total.add(a.getScore());}else{hasSubjective=true;}answers.add(a);}
        try{if(!answers.isEmpty())mapper.batchInsertAnswers(answers);}catch(DuplicateKeyException e){throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);}
        String grading=hasSubjective?"PENDING":"COMPLETED";if(mapper.submitRecord(recordId,now,grading,total)!=1)throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);
        StudentExamSubmitRes res=new StudentExamSubmitRes();res.setRecordId(recordId);res.setExamId(record.getExamId());res.setStatus("SUBMITTED");res.setGradingStatus(grading);res.setSubmitTime(now);
        if(Boolean.TRUE.equals(exam.getAnswerVisible()))res.setScore(total);
        return res;
    }
    public ExamSubmissionDetailRes submission(Long recordId){
        StudentEntity student=currentStudent();StudentExamRecordEntity record=ownedRecord(recordId,student.getId());if(!"SUBMITTED".equals(record.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);
        ExamSubmissionDetailRes res=buildDetail(recordId);if(!Boolean.TRUE.equals(res.getAnswerVisible())){res.setScore(null);res.getQuestions().forEach(q->{q.setCorrect(null);q.setScore(null);q.setCorrectAnswer(null);q.setAnalysisContent(null);});}return res;
    }
    private ExamSubmissionDetailRes buildDetail(Long id){ExamSubmissionDetailRes res=mapper.selectRecordDetail(id);List<ExamSubmissionDetailRes.Question> qs=mapper.selectSubmissionQuestions(id);qs.forEach(q->q.setOptions(options(q.getQuestionId())));res.setQuestions(qs);return res;}
    private StudentExamPaperRes.Question toPaperQuestion(ExamPaperQuestionRow row){StudentExamPaperRes.Question q=new StudentExamPaperRes.Question();q.setId(row.getId());q.setQuestionId(row.getQuestionId());q.setQuestionContent(row.getQuestionContent());q.setQuestionType(row.getQuestionType());q.setDifficulty(row.getDifficulty());q.setQuestionScore(row.getQuestionScore());q.setOptions("JUDGE".equals(row.getQuestionType())?judgeOptions(row.getQuestionId()):options(row.getQuestionId()));return q;}
    private List<StudentExamPaperRes.Option> judgeOptions(Long questionId){StudentExamPaperRes.Option yes=new StudentExamPaperRes.Option();yes.setId(-1L);yes.setQuestionId(questionId);yes.setOptionKey("TRUE");yes.setOptionContent("正确");StudentExamPaperRes.Option no=new StudentExamPaperRes.Option();no.setId(-2L);no.setQuestionId(questionId);no.setOptionKey("FALSE");no.setOptionContent("错误");return List.of(yes,no);}
    private List<StudentExamPaperRes.Option> options(Long questionId){return questionMapper.selectOptions(questionId).stream().map(x->{StudentExamPaperRes.Option o=new StudentExamPaperRes.Option();o.setId(x.getId());o.setQuestionId(x.getQuestionId());o.setOptionKey(x.getOptionKey());o.setOptionContent(x.getOptionContent());return o;}).toList();}
    private String normalizeAnswer(String type,String value){String v=StrUtil.blankToDefault(StrUtil.trim(value),"").toUpperCase(Locale.ROOT);if("MULTIPLE".equals(type))return Arrays.stream(v.split(",")).map(String::trim).filter(s->!s.isEmpty()).distinct().sorted().collect(Collectors.joining(","));return v;}
    private void applyStatus(StudentExamRes x,LocalDateTime now){String status;if("SUBMITTED".equals(x.getRecordStatus()))status="SUBMITTED";else if("TIMEOUT".equals(x.getRecordStatus()))status="TIMEOUT";else if("IN_PROGRESS".equals(x.getRecordStatus())){if(!now.isBefore(x.getDeadlineTime())){mapper.markTimeout(x.getRecordId());x.setRecordStatus("TIMEOUT");status="TIMEOUT";}else status="IN_PROGRESS";}else if(now.isBefore(x.getStartTime()))status="NOT_STARTED";else if(!now.isAfter(x.getEndTime()))status="AVAILABLE";else status="ENDED";x.setExamStatus(status);x.setExamStatusText(Map.of("NOT_STARTED","未开始","AVAILABLE","可进入","IN_PROGRESS","进行中","SUBMITTED","已提交","TIMEOUT","已超时","ENDED","已结束").get(status));x.setCanStart("AVAILABLE".equals(status)||"IN_PROGRESS".equals(status));if(!Boolean.TRUE.equals(x.getAnswerVisible()))x.setScore(null);}
    private void ensureInProgress(StudentExamRecordEntity r){if("SUBMITTED".equals(r.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_ALREADY_SUBMITTED);if(!"IN_PROGRESS".equals(r.getStatus())||!LocalDateTime.now().isBefore(r.getDeadlineTime())){mapper.markTimeout(r.getId());throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_TIMEOUT);}}
    private StudentExamStartRes startRes(StudentExamRecordEntity r,Long paperId){StudentExamStartRes x=new StudentExamStartRes();x.setRecordId(r.getId());x.setExamId(r.getExamId());x.setPaperId(paperId);x.setStartTime(r.getStartTime());x.setDeadlineTime(r.getDeadlineTime());x.setStatus(r.getStatus());return x;}
    private StudentEntity currentStudent(){Long id=UserContext.getUserId();StudentEntity s=id==null?null:studentMapper.selectById(id);if(s==null||!"ACTIVE".equals(s.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_NOT_FOUND);return s;}
    private void requireClass(StudentEntity s){if(s.getClassId()==null)throw BusinessException.of(CommonErrorCode.STUDENT_CLASS_NOT_FOUND);}
    private StudentExamRecordEntity ownedRecord(Long id,Long studentId){StudentExamRecordEntity r=mapper.selectRecord(id);if(r==null)throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_RECORD_NOT_FOUND);if(!studentId.equals(r.getStudentId()))throw BusinessException.of(CommonErrorCode.STUDENT_EXAM_FORBIDDEN);return r;}
    private ExamEntity requireExam(Long id){ExamEntity e=examMapper.selectById(id);if(e==null)throw BusinessException.of(CommonErrorCode.EXAM_NOT_FOUND);return e;}
}
