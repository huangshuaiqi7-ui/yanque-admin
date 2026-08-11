package cn.yanque.modules.exams.service.impl;
import cn.yanque.commons.apires.*; import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.classes.mapper.SysClassMapper; import cn.yanque.modules.exampapers.mapper.ExamPaperMapper;
import cn.yanque.modules.exams.mapper.ExamMapper; import cn.yanque.modules.exams.pojo.entity.ExamEntity;
import cn.yanque.modules.exams.pojo.vo.reqvo.*; import cn.yanque.modules.exams.pojo.vo.resvo.ExamRes; import cn.yanque.modules.exams.service.ExamService;
import cn.yanque.modules.users.mapper.SysUserMapper; import cn.yanque.modules.users.pojo.entity.SysUserEntity;
import com.github.pagehelper.*; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service public class ExamServiceImpl implements ExamService {
    private final ExamMapper mapper; private final ExamPaperMapper paperMapper; private final SysClassMapper classMapper; private final SysUserMapper userMapper;
    public ExamServiceImpl(ExamMapper mapper,ExamPaperMapper paperMapper,SysClassMapper classMapper,SysUserMapper userMapper){this.mapper=mapper;this.paperMapper=paperMapper;this.classMapper=classMapper;this.userMapper=userMapper;}
    public PageResult<ExamRes> page(ExamPageReq req){PageHelper.startPage(req.getPageNum(),req.getPageSize());List<ExamRes> rows=mapper.selectPage(req.getPaperId(),req.getClassId(),req.getInvigilatorUserId());PageInfo<ExamRes> info=new PageInfo<>(rows);return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows);}
    public ExamRes detail(Long id){require(id);return mapper.selectDetail(id);}
    @Transactional public Long create(ExamSaveReq req){validate(req,null);ExamEntity exam=toEntity(req);exam.setAnswerVisible(false);if(mapper.insert(exam)!=1)throw BusinessException.of(CommonErrorCode.EXAM_OPERATION_FAILED);return exam.getId();}
    @Transactional public void update(Long id,ExamSaveReq req){require(id);validate(req,id);ExamEntity exam=toEntity(req);exam.setId(id);if(mapper.updateById(exam)!=1)throw BusinessException.of(CommonErrorCode.EXAM_OPERATION_FAILED);}
    @Transactional public void delete(Long id){require(id);try{if(mapper.deleteById(id)!=1)throw BusinessException.of(CommonErrorCode.EXAM_OPERATION_FAILED);}catch(DataIntegrityViolationException e){throw BusinessException.of(CommonErrorCode.EXAM_OPERATION_FAILED,"考试已经产生学生答题记录，不能删除");}}
    @Transactional public void updateAnswerVisible(Long id,ExamAnswerVisibleReq req){require(id);if(mapper.updateAnswerVisible(id,req.getAnswerVisible())!=1)throw BusinessException.of(CommonErrorCode.EXAM_OPERATION_FAILED);}
    private void validate(ExamSaveReq req,Long excludeId){
        if(!req.getStartTime().isBefore(req.getEndTime()))throw BusinessException.of(CommonErrorCode.EXAM_TIME_INVALID);
        if(paperMapper.selectById(req.getPaperId())==null)throw BusinessException.of(CommonErrorCode.EXAM_PAPER_INVALID);
        if(classMapper.selectById(req.getClassId())==null)throw BusinessException.of(CommonErrorCode.EXAM_CLASS_INVALID);
        SysUserEntity user=userMapper.selectById(req.getInvigilatorUserId());
        if(user==null||!"ACTIVE".equals(user.getStatus()))throw BusinessException.of(CommonErrorCode.EXAM_INVIGILATOR_INVALID);
        if(mapper.countClassTimeConflict(req.getClassId(),req.getStartTime(),req.getEndTime(),excludeId)>0)throw BusinessException.of(CommonErrorCode.EXAM_CLASS_TIME_CONFLICT);
    }
    private ExamEntity toEntity(ExamSaveReq req){ExamEntity e=new ExamEntity();e.setPaperId(req.getPaperId());e.setClassId(req.getClassId());e.setStartTime(req.getStartTime());e.setEndTime(req.getEndTime());e.setDurationMinutes(req.getDurationMinutes());e.setInvigilatorUserId(req.getInvigilatorUserId());return e;}
    private ExamEntity require(Long id){ExamEntity e=mapper.selectById(id);if(e==null)throw BusinessException.of(CommonErrorCode.EXAM_NOT_FOUND);return e;}
}
