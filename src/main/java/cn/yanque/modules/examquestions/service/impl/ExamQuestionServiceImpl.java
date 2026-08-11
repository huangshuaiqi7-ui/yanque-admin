package cn.yanque.modules.examquestions.service.impl;
import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.*;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.courses.mapper.*;
import cn.yanque.modules.examquestions.mapper.ExamQuestionMapper;
import cn.yanque.modules.examquestions.pojo.entity.*;
import cn.yanque.modules.examquestions.pojo.vo.reqvo.*;
import cn.yanque.modules.examquestions.pojo.vo.resvo.*;
import cn.yanque.modules.examquestions.service.ExamQuestionService;
import com.github.pagehelper.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamQuestionServiceImpl implements ExamQuestionService {
    private static final Set<String> TYPES=Set.of("SINGLE","MULTIPLE","JUDGE","FILL","SHORT","PROGRAMMING");
    private static final Set<String> CHOICE_TYPES=Set.of("SINGLE","MULTIPLE");
    private static final Set<String> DIFFICULTIES=Set.of("EASY","NORMAL","HARD");
    private static final Set<String> STATUSES=Set.of("ENABLED","DISABLED");
    private final ExamQuestionMapper mapper; private final SysCourseMapper courseMapper; private final SysCourseDetailMapper detailMapper;
    public ExamQuestionServiceImpl(ExamQuestionMapper mapper,SysCourseMapper courseMapper,SysCourseDetailMapper detailMapper){this.mapper=mapper;this.courseMapper=courseMapper;this.detailMapper=detailMapper;}
    public PageResult<ExamQuestionRes> page(ExamQuestionPageReq req){
        validateFilter(req); PageHelper.startPage(req.getPageNum(),req.getPageSize());
        List<ExamQuestionRes> rows=mapper.selectPage(normal(req.getQuestionType()),req.getCourseId(),StrUtil.trim(req.getStageName()),normal(req.getDifficulty()),normal(req.getStatus()),StrUtil.trim(req.getKeyword()));
        PageInfo<ExamQuestionRes> info=new PageInfo<>(rows); rows.forEach(this::fillCourses);
        return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows);
    }
    public ExamQuestionRes detail(Long id){
        ExamQuestionEntity q=require(id); ExamQuestionRes res=toRes(q); res.setOptions(mapper.selectOptions(id)); fillCourses(res); return res;
    }
    @Transactional public Long create(ExamQuestionSaveReq req){
        ValidatedData data=validate(req); ExamQuestionEntity q=toEntity(req,data);
        if(mapper.insert(q)!=1)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPERATION_FAILED);
        replaceChildren(q.getId(),data); return q.getId();
    }
    @Transactional public void update(Long id,ExamQuestionSaveReq req){
        require(id); ValidatedData data=validate(req); ExamQuestionEntity q=toEntity(req,data);q.setId(id);
        if(mapper.updateById(q)!=1)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPERATION_FAILED);
        mapper.deleteOptions(id);mapper.deleteCourseStages(id);replaceChildren(id,data);
    }
    @Transactional public void delete(Long id){
        require(id);mapper.deleteOptions(id);mapper.deleteCourseStages(id);
        try{if(mapper.deleteById(id)!=1)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPERATION_FAILED);}
        catch(DataIntegrityViolationException e){throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPERATION_FAILED,"题目已被试卷引用，不能删除");}
    }
    @Transactional public void updateStatus(Long id,ExamQuestionStatusReq req){
        require(id);String status=normal(req.getStatus());if(!STATUSES.contains(status))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_STATUS_INVALID);
        if(mapper.updateStatus(id,status)!=1)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPERATION_FAILED);
    }
    private ValidatedData validate(ExamQuestionSaveReq req){
        String type=normal(req.getQuestionType()),difficulty=normal(req.getDifficulty()),status=normal(req.getStatus());
        if(!TYPES.contains(type))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_TYPE_INVALID);
        if(!DIFFICULTIES.contains(difficulty))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_DIFFICULTY_INVALID);
        if(!STATUSES.contains(status))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_STATUS_INVALID);
        List<ExamQuestionSaveReq.Option> raw=Optional.ofNullable(req.getOptions()).orElse(List.of());
        List<ExamQuestionOptionEntity> options=new ArrayList<>();
        if(CHOICE_TYPES.contains(type)){
            if(raw.size()<2)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPTIONS_INVALID);
            Set<String> keys=new LinkedHashSet<>();int sort=0;
            for(ExamQuestionSaveReq.Option item:raw){String key=normal(item.getOptionKey());String content=StrUtil.trim(item.getOptionContent());
                if(StrUtil.isBlank(key)||StrUtil.isBlank(content)||!keys.add(key))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_OPTIONS_INVALID);
                ExamQuestionOptionEntity option=new ExamQuestionOptionEntity();option.setOptionKey(key);option.setOptionContent(content);option.setSortOrder(sort++);options.add(option);}
            Set<String> answers=Arrays.stream(StrUtil.trim(req.getAnswerContent()).split(",")).map(this::normal).filter(StrUtil::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
            if(answers.isEmpty()||!keys.containsAll(answers)||(type.equals("SINGLE")&&answers.size()!=1))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_ANSWER_INVALID);
            req.setAnswerContent(String.join(",",answers));
        }else{
            if(!raw.isEmpty())throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_NON_CHOICE_OPTIONS_FORBIDDEN);
            if(type.equals("JUDGE")&&!Set.of("TRUE","FALSE").contains(normal(req.getAnswerContent())))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_ANSWER_INVALID);
            if(type.equals("JUDGE"))req.setAnswerContent(normal(req.getAnswerContent()));
        }
        List<ExamQuestionSaveReq.CourseStage> rawStages=Optional.ofNullable(req.getCourseStages()).orElse(List.of());
        Set<Long> selectedIds=Optional.ofNullable(req.getCourseIds()).orElse(List.of()).stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> stageCourseIds=new HashSet<>();List<ExamQuestionCourseStageRes> stages=new ArrayList<>();
        for(ExamQuestionSaveReq.CourseStage item:rawStages){String stage=StrUtil.trim(item.getStageName());
            if(!stageCourseIds.add(item.getCourseId()))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_COURSE_REPEATED);
            if(courseMapper.selectById(item.getCourseId())==null||detailMapper.countByCourseIdAndStageName(item.getCourseId(),stage)==0)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_COURSE_STAGE_INVALID);
            ExamQuestionCourseStageRes x=new ExamQuestionCourseStageRes();x.setCourseId(item.getCourseId());x.setStageName(stage);stages.add(x);}
        if(!selectedIds.equals(stageCourseIds))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_COURSE_STAGE_INVALID);
        return new ValidatedData(type,difficulty,status,options,stages);
    }
    private void replaceChildren(Long id,ValidatedData data){
        data.options.forEach(x->x.setQuestionId(id));if(!data.options.isEmpty())mapper.batchInsertOptions(data.options);
        if(!data.stages.isEmpty())mapper.batchInsertCourseStages(id,data.stages);
    }
    private void validateFilter(ExamQuestionPageReq req){
        if(StrUtil.isNotBlank(req.getQuestionType())&&!TYPES.contains(normal(req.getQuestionType())))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_TYPE_INVALID);
        if(StrUtil.isNotBlank(req.getDifficulty())&&!DIFFICULTIES.contains(normal(req.getDifficulty())))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_DIFFICULTY_INVALID);
        if(StrUtil.isNotBlank(req.getStatus())&&!STATUSES.contains(normal(req.getStatus())))throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_STATUS_INVALID);
    }
    private void fillCourses(ExamQuestionRes res){List<ExamQuestionCourseStageRes> items=mapper.selectCourseStages(res.getId());res.setCourseIds(items.stream().map(ExamQuestionCourseStageRes::getCourseId).toList());res.setCourseStageKeys(items.stream().map(x->x.getCourseId()+"::"+x.getStageName()).toList());if(StrUtil.isBlank(res.getCourseNames()))res.setCourseNames(items.stream().map(ExamQuestionCourseStageRes::getCourseName).collect(Collectors.joining("、")));if(StrUtil.isBlank(res.getCourseStageNames()))res.setCourseStageNames(items.stream().map(x->x.getCourseName()+" / "+x.getStageName()).collect(Collectors.joining("、")));}
    private ExamQuestionEntity require(Long id){ExamQuestionEntity q=mapper.selectById(id);if(q==null)throw BusinessException.of(CommonErrorCode.EXAM_QUESTION_NOT_FOUND);return q;}
    private ExamQuestionEntity toEntity(ExamQuestionSaveReq req,ValidatedData d){ExamQuestionEntity q=new ExamQuestionEntity();q.setQuestionType(d.type);q.setQuestionContent(StrUtil.trim(req.getQuestionContent()));q.setAnswerContent(StrUtil.trim(req.getAnswerContent()));q.setAnalysisContent(StrUtil.trim(req.getAnalysisContent()));q.setDifficulty(d.difficulty);q.setStatus(d.status);return q;}
    private ExamQuestionRes toRes(ExamQuestionEntity q){ExamQuestionRes r=new ExamQuestionRes();r.setId(q.getId());r.setQuestionType(q.getQuestionType());r.setQuestionContent(q.getQuestionContent());r.setAnswerContent(q.getAnswerContent());r.setAnalysisContent(q.getAnalysisContent());r.setDifficulty(q.getDifficulty());r.setStatus(q.getStatus());r.setCreatedAt(q.getCreatedAt());r.setUpdatedAt(q.getUpdatedAt());return r;}
    private String normal(String value){String trimmed=StrUtil.trim(value);return trimmed==null?null:trimmed.toUpperCase(Locale.ROOT);}
    private record ValidatedData(String type,String difficulty,String status,List<ExamQuestionOptionEntity> options,List<ExamQuestionCourseStageRes> stages){}
}
