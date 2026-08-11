package cn.yanque.modules.homeworks.service;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.*;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.classes.mapper.*;
import cn.yanque.modules.classes.pojo.entity.*;
import cn.yanque.modules.homeworks.mapper.HomeworkMapper;
import cn.yanque.modules.homeworks.pojo.entity.HomeworkEntity;
import cn.yanque.modules.homeworks.pojo.vo.reqvo.*;
import cn.yanque.modules.homeworks.pojo.vo.resvo.*;
import com.github.pagehelper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class HomeworkService {
    private final HomeworkMapper mapper;
    private final SysClassMapper classMapper;
    private final SysClassScheduleMapper scheduleMapper;
    public HomeworkService(HomeworkMapper mapper, SysClassMapper classMapper, SysClassScheduleMapper scheduleMapper) {
        this.mapper=mapper; this.classMapper=classMapper; this.scheduleMapper=scheduleMapper;
    }
    public PageResult<HomeworkRes> page(HomeworkPageReq req) {
        PageHelper.startPage(req.getPageNum(),req.getPageSize());
        List<HomeworkRes> list=mapper.selectPage(StrUtil.trim(req.getTitle()),req.getClassId(),req.getHomeworkDate());
        PageInfo<HomeworkRes> info=new PageInfo<>(list);
        return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),list);
    }
    public HomeworkPrepareRes prepare(Long classId, LocalDate date) {
        SysClassEntity clazz=requireClass(classId);
        ensureNotDuplicated(classId,date);
        SysClassScheduleEntity schedule=requireSchedule(classId,date);
        HomeworkPrepareRes res=new HomeworkPrepareRes();
        res.setClassId(classId); res.setClassPeriod(clazz.getClassPeriod()); res.setHomeworkDate(date);
        res.setClassContent(schedule.getCourseContent()); res.setDefaultTitle(clazz.getClassPeriod()+" "+date+" 作业");
        res.setStartTime(date.atStartOfDay()); res.setDeadline(date.atTime(23,59,59));
        return res;
    }
    @Transactional
    public Long create(HomeworkCreateReq req) {
        requireClass(req.getClassId()); ensureNotDuplicated(req.getClassId(),req.getHomeworkDate());
        SysClassScheduleEntity schedule=requireSchedule(req.getClassId(),req.getHomeworkDate());
        if(req.getDeadline().isBefore(req.getStartTime())) throw BusinessException.of(CommonErrorCode.HOMEWORK_TIME_INVALID);
        validateDocument(req.getContentObjectKey(),req.getContentFileName(),"homework/content/"+req.getClassId()+"/");
        HomeworkEntity h=new HomeworkEntity();
        h.setTitle(req.getTitle().trim()); h.setContentObjectKey(req.getContentObjectKey()); h.setContentFileName(req.getContentFileName());
        h.setClassId(req.getClassId()); h.setHomeworkDate(req.getHomeworkDate()); h.setClassContent(schedule.getCourseContent());
        h.setStartTime(req.getStartTime()); h.setDeadline(req.getDeadline()); h.setRemark(req.getRemark());
        if(mapper.insert(h)!=1) throw BusinessException.of(CommonErrorCode.HOMEWORK_OPERATION_FAILED);
        return h.getId();
    }
    @Transactional
    public void publishAnswer(Long id, HomeworkAnswerReq req) {
        requireHomework(id); validateDocument(req.getAnswerObjectKey(),req.getAnswerFileName(),"homework/answer/"+id+"/");
        if(mapper.updateAnswer(id,req.getAnswerObjectKey(),req.getAnswerFileName(),req.getAnswerStudentVisible())!=1)
            throw BusinessException.of(CommonErrorCode.HOMEWORK_OPERATION_FAILED);
    }
    public PageResult<HomeworkSubmissionRes> submissions(Long id,int pageNum,int pageSize) {
        requireHomework(id); PageHelper.startPage(pageNum,pageSize);
        List<HomeworkSubmissionRes> list=mapper.selectSubmissions(id); PageInfo<HomeworkSubmissionRes> info=new PageInfo<>(list);
        return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),list);
    }
    @Transactional
    public void grade(Long id, HomeworkGradeReq req) {
        if(mapper.selectSubmissionById(id)==null) throw BusinessException.of(CommonErrorCode.HOMEWORK_SUBMISSION_NOT_FOUND);
        if(mapper.gradeSubmission(id,req.getScore(),req.getTeacherRemark())!=1) throw BusinessException.of(CommonErrorCode.HOMEWORK_OPERATION_FAILED);
    }
    public HomeworkEntity requireHomework(Long id) {
        HomeworkEntity h=mapper.selectById(id); if(h==null) throw BusinessException.of(CommonErrorCode.HOMEWORK_NOT_FOUND); return h;
    }
    private SysClassEntity requireClass(Long id) {
        SysClassEntity c=classMapper.selectById(id); if(c==null) throw BusinessException.of(CommonErrorCode.CLASS_NOT_FOUND); return c;
    }
    private SysClassScheduleEntity requireSchedule(Long classId,LocalDate date) {
        SysClassScheduleEntity s=scheduleMapper.selectByClassIdAndDate(classId,date);
        if(s==null) throw BusinessException.of(CommonErrorCode.HOMEWORK_SCHEDULE_NOT_FOUND); return s;
    }
    private void ensureNotDuplicated(Long classId,LocalDate date) {
        if(mapper.countByClassAndDate(classId,date)>0) throw BusinessException.of(CommonErrorCode.HOMEWORK_DUPLICATED);
    }
    public void validateDocument(String key,String name,String prefix) {
        String k=StrUtil.trim(key); String n=StrUtil.trim(name);
        if(StrUtil.isBlank(k)||StrUtil.isBlank(n)||!k.startsWith(prefix)||!k.toLowerCase().endsWith(".md")||!n.toLowerCase().endsWith(".md")||k.contains("..")||k.contains("\\"))
            throw BusinessException.of(CommonErrorCode.HOMEWORK_DOCUMENT_INVALID);
    }
}
