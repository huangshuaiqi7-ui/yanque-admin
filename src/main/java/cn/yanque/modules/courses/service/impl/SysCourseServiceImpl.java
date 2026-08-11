package cn.yanque.modules.courses.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.enums.TeachingModeEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.courses.mapper.SysCourseDetailMapper;
import cn.yanque.modules.courses.mapper.SysCourseMapper;
import cn.yanque.modules.courses.pojo.entity.SysCourseDetailEntity;
import cn.yanque.modules.courses.pojo.entity.SysCourseEntity;
import cn.yanque.modules.courses.pojo.excel.CourseDetailImportRow;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseDetailSaveReq;
import cn.yanque.modules.courses.pojo.vo.reqvo.CoursePageReq;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseSaveReq;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseDetailRes;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseRes;
import cn.yanque.modules.courses.service.SysCourseService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SysCourseServiceImpl implements SysCourseService {
    private final SysCourseMapper courseMapper;
    private final SysCourseDetailMapper detailMapper;

    public SysCourseServiceImpl(SysCourseMapper courseMapper, SysCourseDetailMapper detailMapper) {
        this.courseMapper = courseMapper;
        this.detailMapper = detailMapper;
    }

    @Override
    public PageResult<CourseRes> page(CoursePageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SysCourseEntity> courses = courseMapper.selectPage(req.getKeyword());
        PageInfo<SysCourseEntity> pageInfo = new PageInfo<>(courses);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(),
                courses.stream().map(this::toCourseRes).toList());
    }

    @Override
    public CourseRes detail(Long id) {
        return toCourseRes(getCourse(id));
    }

    @Override
    @Transactional
    public Long create(CourseSaveReq req) {
        SysCourseEntity course = toCourseEntity(req);
        if (courseMapper.insert(course) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_OPERATION_FAILED);
        }
        return course.getId();
    }

    @Override
    @Transactional
    public void update(Long id, CourseSaveReq req) {
        SysCourseEntity existing = getCourse(id);
        int detailCount = detailMapper.countByCourseId(id);
        if (!existing.getTeachingMode().equals(req.getTeachingMode()) && detailCount > 0) {
            throw BusinessException.of(CommonErrorCode.COURSE_MODE_CHANGE_HAS_DETAILS);
        }
        Integer maxDayNumber = detailMapper.selectMaxDayNumber(id);
        if (maxDayNumber != null && maxDayNumber > req.getCourseDays()) {
            throw BusinessException.of(CommonErrorCode.COURSE_DAYS_LESS_THAN_DETAIL);
        }
        SysCourseEntity course = toCourseEntity(req);
        course.setId(id);
        if (courseMapper.updateById(course) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_OPERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getCourse(id);
        if (courseMapper.countClassReferences(id) > 0) {
            throw BusinessException.of(CommonErrorCode.COURSE_REFERENCED_BY_CLASS);
        }
        detailMapper.deleteByCourseId(id);
        if (courseMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_OPERATION_FAILED);
        }
    }

    @Override
    public List<CourseDetailRes> listDetails(Long courseId) {
        getCourse(courseId);
        return detailMapper.selectByCourseId(courseId).stream().map(this::toDetailRes).toList();
    }

    @Override
    public List<String> listStages(Long courseId) {
        getCourse(courseId);
        return detailMapper.selectStageNames(courseId);
    }

    @Override
    public CourseDetailRes detailItem(Long id) {
        return toDetailRes(getDetail(id));
    }

    @Override
    @Transactional
    public Long createDetail(Long courseId, CourseDetailSaveReq req) {
        SysCourseEntity course = getCourse(courseId);
        validateDetail(course, req);
        validateStage(courseId, req.getStageName());
        shiftOfflineDays(course, req.getDayNumber());
        SysCourseDetailEntity detail = toDetailEntity(req);
        detail.setCourseId(courseId);
        if (detailMapper.insert(detail) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_OPERATION_FAILED);
        }
        return detail.getId();
    }

    @Override
    @Transactional
    public void updateDetail(Long id, CourseDetailSaveReq req) {
        SysCourseDetailEntity existing = getDetail(id);
        SysCourseEntity course = getCourse(existing.getCourseId());
        validateDetail(course, req);
        validateStage(existing.getCourseId(), req.getStageName());
        SysCourseDetailEntity detail = toDetailEntity(req);
        detail.setId(id);
        if (detailMapper.updateById(detail) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_OPERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteDetail(Long id) {
        getDetail(id);
        if (detailMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_OPERATION_FAILED);
        }
    }

    /*
      courseId: 课程ID
     */
    @Override
    @Transactional
    public void importDetails(Long courseId, MultipartFile file) {
        //  根据前台传递课程ID,获取课程,
        SysCourseEntity course = getCourse(courseId);
        //  对导入的Excel文件进行校验
        validateImportFile(file);

        //判断是否是线下课程.
        if (!TeachingModeEnum.OFFLINE.name().equals(course.getTeachingMode())) {
            throw BusinessException.of(CommonErrorCode.COURSE_IMPORT_ONLY_OFFLINE);
        }

        // 必须先完成全部读取和校验，校验失败时不能删除数据库原数据。
        List<CourseDetailImportRow> rows = readImportRows(file);

        // 对导入的行进行校验, 把读取到的数据转换到了数据库的实体对象中。(Entity)
        List<SysCourseDetailEntity> details = validateAndConvertImportRows(course, rows);

        // 删除课程详情的原数据
        detailMapper.deleteByCourseId(courseId);
        // 校验: 批量插入的数据和读取到List集合的数据, 保持一致.
        if (detailMapper.batchInsert(details) != details.size()) {// details.size() 读取到Excel当中的行数.
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_IMPORT_FAILED);
        }
    }

    private void validateImportFile(MultipartFile file) {
        // 文件不能为空,如果为空, 直接抛出异常
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(CommonErrorCode.COURSE_IMPORT_FILE_EMPTY);
        }
        //获取了原始文件的名称.
        String fileName = StrUtil.nullToEmpty(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        // 判断文件类型.必须excel类型.   MIME 类型. 上传的Excel 也有MIME类型. (开发当中, 使用MIME类型进行校验)
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            throw BusinessException.of(CommonErrorCode.COURSE_IMPORT_FILE_TYPE_INVALID);
        }
    }

    private List<CourseDetailImportRow> readImportRows(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())//读取的数据封装到流对象.
                    .head(CourseDetailImportRow.class)// 标题栏
                    .autoCloseStream(true)//自动关闭流
                    .sheet(0)// 指定读取sheet
                    .headRowNumber(1)//数据行从第一行开始.
                    .doReadSync();// 同步读取.
        } catch (IOException exception) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_IMPORT_FAILED, "Excel文件读取失败");
        } catch (RuntimeException exception) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_IMPORT_FAILED,
                    "Excel格式不正确，请使用课程详情导入模板");
        }
    }

    private List<SysCourseDetailEntity> validateAndConvertImportRows(
            SysCourseEntity course, List<CourseDetailImportRow> rows) {
        if (rows.isEmpty()) {
            throw BusinessException.of(CommonErrorCode.COURSE_IMPORT_DATA_EMPTY);
        }
        if (rows.size() > course.getCourseDays()) {
            throw BusinessException.of(CommonErrorCode.COURSE_IMPORT_DATA_INVALID,
                    "Excel课程天数不能超过课程总天数" + course.getCourseDays());
        }

        List<SysCourseDetailEntity> details = new ArrayList<>(rows.size());
        Set<String> completedStages = new HashSet<>();
        String currentStage = null;
        for (int index = 0; index < rows.size(); index++) {
            CourseDetailImportRow row = rows.get(index);
            int excelRowNumber = index + 2;
            String stageName = StrUtil.trim(row.getStageName());
            String classContent = StrUtil.trim(row.getClassContent());

            if (StrUtil.isBlank(stageName)) {
                throw importRowError(excelRowNumber, "阶段名称不能为空");
            }
            if (stageName.length() > 128) {
                throw importRowError(excelRowNumber, "阶段名称长度不能超过128个字符");
            }
            if (!stageName.equals(currentStage)) {
                if (currentStage != null) {
                    completedStages.add(currentStage);
                }
                if (completedStages.contains(stageName)) {
                    throw importRowError(excelRowNumber, "同一阶段必须连续，阶段“" + stageName + "”重复出现");
                }
                currentStage = stageName;
            }

            int expectedDayNumber = index + 1;
            if (row.getDayNumber() == null) {
                throw importRowError(excelRowNumber, "第几天不能为空");
            }
            if (row.getDayNumber() != expectedDayNumber) {
                throw importRowError(excelRowNumber,
                        "第几天必须从1开始连续且不能重复，当前应填写" + expectedDayNumber);
            }
            if (StrUtil.isBlank(classContent)) {
                throw importRowError(excelRowNumber, "上课内容不能为空");
            }
            if (classContent.length() > 1000) {
                throw importRowError(excelRowNumber, "上课内容长度不能超过1000个字符");
            }

            //对应数据库的实体:
            SysCourseDetailEntity detail = new SysCourseDetailEntity();
            detail.setCourseId(course.getId());
            detail.setStageName(stageName);
            detail.setDayNumber(row.getDayNumber());
            detail.setClassContent(classContent);
            details.add(detail);
        }
        return details;
    }

    private BusinessException importRowError(int rowNumber, String message) {
        return BusinessException.of(CommonErrorCode.COURSE_IMPORT_DATA_INVALID,
                "Excel第" + rowNumber + "行：" + message);
    }

    private void validateDetail(SysCourseEntity course, CourseDetailSaveReq req) {
        if (TeachingModeEnum.ONLINE.name().equals(course.getTeachingMode())) {
            if (req.getDayNumber() != null || req.getClassContent() != null) {
                throw BusinessException.of(CommonErrorCode.ONLINE_COURSE_DETAIL_INVALID);
            }
            return;
        }
        if (req.getDayNumber() == null || StrUtil.isBlank(req.getClassContent())) {
            throw BusinessException.of(CommonErrorCode.OFFLINE_COURSE_DETAIL_REQUIRED);
        }
        if (req.getDayNumber() > course.getCourseDays()) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_DAY_OUT_OF_RANGE);
        }
    }

    private void validateStage(Long courseId, String stageName) {
        if (detailMapper.countByCourseIdAndStageName(courseId, stageName) == 0) {
            throw BusinessException.of(CommonErrorCode.COURSE_STAGE_NOT_FOUND);
        }
    }

    private void shiftOfflineDays(SysCourseEntity course, Integer dayNumber) {
        if (!TeachingModeEnum.OFFLINE.name().equals(course.getTeachingMode())) {
            return;
        }
        Integer maxDayNumber = detailMapper.selectMaxDayNumber(course.getId());
        if (maxDayNumber != null && maxDayNumber >= dayNumber) {
            if (maxDayNumber >= course.getCourseDays()) {
                throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_SHIFT_OUT_OF_RANGE);
            }
            detailMapper.incrementDayNumbersFrom(course.getId(), dayNumber);
        }
    }

    private SysCourseEntity getCourse(Long id) {
        SysCourseEntity course = courseMapper.selectById(id);
        //如果没有获取到直接抛出异常
        if (course == null) {
            throw BusinessException.of(CommonErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    private SysCourseDetailEntity getDetail(Long id) {
        SysCourseDetailEntity detail = detailMapper.selectById(id);
        if (detail == null) {
            throw BusinessException.of(CommonErrorCode.COURSE_DETAIL_NOT_FOUND);
        }
        return detail;
    }

    private SysCourseEntity toCourseEntity(CourseSaveReq req) {
        SysCourseEntity course = new SysCourseEntity();
        course.setCourseName(req.getCourseName());
        course.setCourseDays(req.getCourseDays());
        course.setTeachingMode(req.getTeachingMode());
        course.setMaterialPath(req.getMaterialPath());
        return course;
    }

    private SysCourseDetailEntity toDetailEntity(CourseDetailSaveReq req) {
        SysCourseDetailEntity detail = new SysCourseDetailEntity();
        detail.setStageName(req.getStageName());
        detail.setDayNumber(req.getDayNumber());
        detail.setClassContent(req.getClassContent());
        return detail;
    }

    private CourseRes toCourseRes(SysCourseEntity course) {
        CourseRes result = new CourseRes();
        result.setId(course.getId());
        result.setCourseName(course.getCourseName());
        result.setCourseDays(course.getCourseDays());
        result.setTeachingMode(course.getTeachingMode());
        result.setMaterialPath(course.getMaterialPath());
        result.setCreatedAt(course.getCreatedAt());
        result.setUpdatedAt(course.getUpdatedAt());
        return result;
    }

    private CourseDetailRes toDetailRes(SysCourseDetailEntity detail) {
        CourseDetailRes result = new CourseDetailRes();
        result.setId(detail.getId());
        result.setCourseId(detail.getCourseId());
        result.setStageName(detail.getStageName());
        result.setDayNumber(detail.getDayNumber());
        result.setClassContent(detail.getClassContent());
        result.setCreatedAt(detail.getCreatedAt());
        result.setUpdatedAt(detail.getUpdatedAt());
        return result;
    }
}
