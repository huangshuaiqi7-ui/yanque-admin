package cn.yanque.modules.exampapers.service.impl;
import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.*; import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.courses.mapper.*; import cn.yanque.modules.examquestions.mapper.ExamQuestionMapper;
import cn.yanque.modules.examquestions.pojo.entity.ExamQuestionEntity;
import cn.yanque.modules.exampapers.mapper.ExamPaperMapper; import cn.yanque.modules.exampapers.pojo.entity.*;
import cn.yanque.modules.exampapers.pojo.vo.reqvo.*; import cn.yanque.modules.exampapers.pojo.vo.resvo.ExamPaperRes; import cn.yanque.modules.exampapers.service.ExamPaperService;
import com.github.pagehelper.*; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.util.*;
@Service public class ExamPaperServiceImpl implements ExamPaperService {
    private final ExamPaperMapper mapper; private final SysCourseMapper courseMapper; private final SysCourseDetailMapper detailMapper; private final ExamQuestionMapper questionMapper;
    public ExamPaperServiceImpl(ExamPaperMapper mapper,SysCourseMapper courseMapper,SysCourseDetailMapper detailMapper,ExamQuestionMapper questionMapper){this.mapper=mapper;this.courseMapper=courseMapper;this.detailMapper=detailMapper;this.questionMapper=questionMapper;}
    public PageResult<ExamPaperRes> page(ExamPaperPageReq req){
        PageHelper.startPage(req.getPageNum(),req.getPageSize());List<ExamPaperRes> rows=mapper.selectPage(req.getCourseId(),StrUtil.trim(req.getStageName()),StrUtil.trim(req.getPaperName()));
        PageInfo<ExamPaperRes> info=new PageInfo<>(rows);return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows);
    }
    public ExamPaperRes detail(Long id){require(id);ExamPaperRes res=mapper.selectDetail(id);res.setQuestions(mapper.selectQuestions(id));return res;}
    @Transactional public Long create(ExamPaperSaveReq req){
        if(courseMapper.selectById(req.getCourseId())==null)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_COURSE_INVALID);
        String stage=StrUtil.trim(req.getStageName());
        if(StrUtil.isNotBlank(stage)&&detailMapper.countByCourseIdAndStageName(req.getCourseId(),stage)==0)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_STAGE_INVALID);
        List<ExamPaperSaveReq.Question> questions=req.getQuestions();
        if(questions==null||questions.isEmpty())throw BusinessException.of(CommonErrorCode.EXAM_PAPER_QUESTION_EMPTY);
        Set<Long> ids=new HashSet<>();BigDecimal sum=BigDecimal.ZERO;
        for(ExamPaperSaveReq.Question item:questions){
            if(!ids.add(item.getQuestionId()))throw BusinessException.of(CommonErrorCode.EXAM_PAPER_QUESTION_REPEATED);
            if(item.getQuestionScore()==null||item.getQuestionScore().compareTo(BigDecimal.ZERO)<=0)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_SCORE_INVALID);
            ExamQuestionEntity question=questionMapper.selectById(item.getQuestionId());
            if(question==null||!"ENABLED".equals(question.getStatus()))throw BusinessException.of(CommonErrorCode.EXAM_PAPER_QUESTION_INVALID);
            if(questionMapper.countQuestionScope(item.getQuestionId(),req.getCourseId(),stage)==0)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_QUESTION_SCOPE_INVALID);
            sum=sum.add(item.getQuestionScore());
        }
        if(sum.compareTo(req.getTotalScore())!=0)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_TOTAL_SCORE_MISMATCH);
        ExamPaperEntity paper=new ExamPaperEntity();paper.setPaperName(StrUtil.trim(req.getPaperName()));paper.setCourseId(req.getCourseId());paper.setStageName(StrUtil.emptyToNull(stage));paper.setTotalScore(req.getTotalScore());
        if(mapper.insert(paper)!=1)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_OPERATION_FAILED);
        List<ExamPaperQuestionEntity> items=questions.stream().map(x->{ExamPaperQuestionEntity e=new ExamPaperQuestionEntity();e.setPaperId(paper.getId());e.setQuestionId(x.getQuestionId());e.setQuestionScore(x.getQuestionScore());return e;}).toList();
        try{if(mapper.batchInsertQuestions(items)!=items.size())throw BusinessException.of(CommonErrorCode.EXAM_PAPER_OPERATION_FAILED);}
        catch(DataIntegrityViolationException e){throw BusinessException.of(CommonErrorCode.EXAM_PAPER_QUESTION_REPEATED);}
        return paper.getId();
    }
    @Transactional public void delete(Long id){require(id);mapper.deleteQuestions(id);try{if(mapper.deleteById(id)!=1)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_OPERATION_FAILED);}catch(DataIntegrityViolationException e){throw BusinessException.of(CommonErrorCode.EXAM_PAPER_OPERATION_FAILED,"试卷已经用于考试，不能删除");}}
    private ExamPaperEntity require(Long id){ExamPaperEntity p=mapper.selectById(id);if(p==null)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_NOT_FOUND);return p;}
}
