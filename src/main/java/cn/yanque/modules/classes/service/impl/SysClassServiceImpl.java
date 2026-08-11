package cn.yanque.modules.classes.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.classes.mapper.SysClassMapper;
import cn.yanque.modules.classes.pojo.entity.SysClassEntity;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassPageReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassSaveReq;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassRes;
import cn.yanque.modules.classes.service.SysClassService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SysClassServiceImpl implements SysClassService {
    private final SysClassMapper classMapper;

    public SysClassServiceImpl(SysClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    @Override
    public PageResult<ClassRes> page(ClassPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<ClassRes> classes = classMapper.selectPage(StrUtil.trim(req.getKeyword()),
                req.getHeadTeacherId(), req.getCampusId(), req.getCourseId());
        PageInfo<ClassRes> pageInfo = new PageInfo<>(classes);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), classes);
    }

    @Override
    public ClassRes detail(Long id) {
        getClassEntity(id);
        return classMapper.selectDetailById(id);
    }

    @Override
    @Transactional
    public Long create(ClassSaveReq req) {
        validateReferences(req);
        validateClassPeriod(req.getClassPeriod(), null);
        SysClassEntity clazz = toEntity(req);
        if (classMapper.insert(clazz) != 1) {
            throw BusinessException.of(CommonErrorCode.CLASS_OPERATION_FAILED);
        }
        return clazz.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ClassSaveReq req) {
        getClassEntity(id);
        validateReferences(req);
        validateClassPeriod(req.getClassPeriod(), id);
        SysClassEntity clazz = toEntity(req);
        clazz.setId(id);
        if (classMapper.updateById(clazz) != 1) {
            throw BusinessException.of(CommonErrorCode.CLASS_OPERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getClassEntity(id);
        if (classMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.CLASS_OPERATION_FAILED);
        }
    }

    private SysClassEntity getClassEntity(Long id) {
        SysClassEntity clazz = classMapper.selectById(id);
        if (clazz == null) {
            throw BusinessException.of(CommonErrorCode.CLASS_NOT_FOUND);
        }
        return clazz;
    }

    private void validateReferences(ClassSaveReq req) {
        if (classMapper.countValidHeadTeacher(req.getHeadTeacherId()) == 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_HEAD_TEACHER_INVALID);
        }
        if (classMapper.countCampus(req.getCampusId()) == 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_CAMPUS_INVALID);
        }
        if (classMapper.countCourse(req.getCourseId()) == 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_COURSE_INVALID);
        }
    }

    private void validateClassPeriod(String classPeriod, Long excludeId) {
        if (classMapper.countByClassPeriod(StrUtil.trim(classPeriod), excludeId) > 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_PERIOD_EXISTS);
        }
    }

    private SysClassEntity toEntity(ClassSaveReq req) {
        SysClassEntity clazz = new SysClassEntity();
        clazz.setClassPeriod(StrUtil.trim(req.getClassPeriod()));
        clazz.setHeadTeacherId(req.getHeadTeacherId());
        clazz.setCampusId(req.getCampusId());
        clazz.setCourseId(req.getCourseId());
        return clazz;
    }
}
