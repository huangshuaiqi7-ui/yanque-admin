package cn.yanque.modules.courses.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.enums.TeachingModeEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.courses.mapper.CourseHomeworkTemplateMapper;
import cn.yanque.modules.courses.mapper.SysCourseDetailMapper;
import cn.yanque.modules.courses.mapper.SysCourseMapper;
import cn.yanque.modules.courses.pojo.entity.CourseHomeworkTemplateEntity;
import cn.yanque.modules.courses.pojo.entity.SysCourseEntity;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseHomeworkTemplateSaveReq;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseHomeworkTemplateRes;
import cn.yanque.modules.courses.service.CourseHomeworkTemplateService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CourseHomeworkTemplateServiceImpl implements CourseHomeworkTemplateService {
    private static final String TEMPLATE_OBJECT_KEY_PREFIX = "course/homework-template/";
    private static final String MARKDOWN_EXTENSION = ".md";

    private final CourseHomeworkTemplateMapper templateMapper;
    private final SysCourseMapper courseMapper;
    private final SysCourseDetailMapper courseDetailMapper;

    public CourseHomeworkTemplateServiceImpl(CourseHomeworkTemplateMapper templateMapper,
                                              SysCourseMapper courseMapper,
                                              SysCourseDetailMapper courseDetailMapper) {
        this.templateMapper = templateMapper;
        this.courseMapper = courseMapper;
        this.courseDetailMapper = courseDetailMapper;
    }

    @Override
    public List<CourseHomeworkTemplateRes> list(Long courseId) {
        getCourse(courseId);
        return templateMapper.selectByCourseId(courseId).stream().map(this::toRes).toList();
    }

    @Override
    public CourseHomeworkTemplateRes detail(Long id) {
        return toRes(getTemplate(id));
    }

    @Override
    @Transactional
    public Long create(Long courseId, CourseHomeworkTemplateSaveReq req) {
        SysCourseEntity course = getCourse(courseId);
        CourseHomeworkTemplateEntity template = buildAndValidate(course, req, null);
        try {
            if (templateMapper.insert(template) != 1) {
                throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_DUPLICATED);
        }
        return template.getId();
    }

    @Override
    @Transactional
    public void update(Long id, CourseHomeworkTemplateSaveReq req) {
        CourseHomeworkTemplateEntity existing = getTemplate(id);
        SysCourseEntity course = getCourse(existing.getCourseId());
        CourseHomeworkTemplateEntity template = buildAndValidate(course, req, id);
        template.setId(id);
        try {
            if (templateMapper.updateById(template) != 1) {
                throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_DUPLICATED);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getTemplate(id);
        if (templateMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_OPERATION_FAILED);
        }
    }

    private CourseHomeworkTemplateEntity buildAndValidate(SysCourseEntity course,
                                                            CourseHomeworkTemplateSaveReq req,
                                                            Long excludeId) {
        validateDocument(req);
        CourseHomeworkTemplateEntity template = new CourseHomeworkTemplateEntity();
        template.setCourseId(course.getId());
        template.setTeachingMode(course.getTeachingMode());

        if (TeachingModeEnum.ONLINE.name().equals(course.getTeachingMode())) {
            String stageName = StrUtil.trim(req.getStageName());
            if (StrUtil.isBlank(stageName) || req.getDayNumber() != null
                    || courseDetailMapper.countByCourseIdAndStageName(course.getId(), stageName) == 0) {
                throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_ONLINE_DIMENSION_INVALID);
            }
            template.setStageName(stageName);
            template.setDayNumber(null);
        } else if (TeachingModeEnum.OFFLINE.name().equals(course.getTeachingMode())) {
            if (req.getDayNumber() == null || StrUtil.isNotBlank(req.getStageName())
                    || courseDetailMapper.countByCourseIdAndDayNumber(course.getId(), req.getDayNumber()) == 0) {
                throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_OFFLINE_DIMENSION_INVALID);
            }
            template.setStageName(null);
            template.setDayNumber(req.getDayNumber());
        } else {
            throw BusinessException.of(CommonErrorCode.COURSE_OPERATION_FAILED);
        }

        if (templateMapper.countDimension(course.getId(), course.getTeachingMode(),
                template.getStageName(), template.getDayNumber(), excludeId) > 0) {
            throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_DUPLICATED);
        }

        template.setContentObjectKey(StrUtil.trim(req.getContentObjectKey()));
        template.setContentFileName(StrUtil.trim(req.getContentFileName()));
        template.setStatus(req.getStatus());
        template.setRemark(StrUtil.trim(req.getRemark()));
        return template;
    }

    private void validateDocument(CourseHomeworkTemplateSaveReq req) {
        String objectKey = StrUtil.trim(req.getContentObjectKey());
        String fileName = StrUtil.trim(req.getContentFileName());
        if (!objectKey.startsWith(TEMPLATE_OBJECT_KEY_PREFIX)
                || !StrUtil.endWithIgnoreCase(objectKey, MARKDOWN_EXTENSION)
                || !StrUtil.endWithIgnoreCase(fileName, MARKDOWN_EXTENSION)
                || objectKey.contains("..") || objectKey.contains("\\")) {
            throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_DOCUMENT_INVALID);
        }
    }

    private SysCourseEntity getCourse(Long courseId) {
        SysCourseEntity course = courseMapper.selectById(courseId);
        if (course == null) {
            throw BusinessException.of(CommonErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    private CourseHomeworkTemplateEntity getTemplate(Long id) {
        CourseHomeworkTemplateEntity template = templateMapper.selectById(id);
        if (template == null) {
            throw BusinessException.of(CommonErrorCode.COURSE_HOMEWORK_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    private CourseHomeworkTemplateRes toRes(CourseHomeworkTemplateEntity template) {
        CourseHomeworkTemplateRes result = new CourseHomeworkTemplateRes();
        result.setId(template.getId());
        result.setCourseId(template.getCourseId());
        result.setTeachingMode(template.getTeachingMode());
        result.setStageName(template.getStageName());
        result.setDayNumber(template.getDayNumber());
        result.setContentObjectKey(template.getContentObjectKey());
        result.setContentFileName(template.getContentFileName());
        result.setStatus(template.getStatus());
        result.setRemark(template.getRemark());
        result.setCreatedAt(template.getCreatedAt());
        result.setUpdatedAt(template.getUpdatedAt());
        return result;
    }
}
