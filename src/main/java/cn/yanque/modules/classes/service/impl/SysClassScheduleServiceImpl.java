package cn.yanque.modules.classes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.enums.ClassScheduleTypeEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.classes.mapper.SysClassMapper;
import cn.yanque.modules.classes.mapper.SysClassScheduleMapper;
import cn.yanque.modules.classes.pojo.config.TeachingScheduleRule;
import cn.yanque.modules.classes.pojo.entity.SysClassEntity;
import cn.yanque.modules.classes.pojo.entity.SysClassScheduleEntity;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleGenerateReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleTeacherAssignReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleAddCourseReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.TeacherScheduleQueryReq;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleGenerateRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleDateDetailRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleTeacherAssignRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassStageInfoRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassStageRangeRes;
import cn.yanque.modules.classes.pojo.vo.resvo.TeacherOptionRes;
import cn.yanque.modules.classes.pojo.vo.resvo.TeacherScheduleDetailRes;
import cn.yanque.modules.classes.pojo.vo.resvo.TeacherScheduleItemRes;
import cn.yanque.modules.classes.pojo.vo.resvo.TeacherScheduleRowRes;
import cn.yanque.modules.classes.service.SysClassScheduleService;
import cn.yanque.modules.classes.service.HolidayCalendarService;
import cn.yanque.modules.courses.mapper.SysCourseDetailMapper;
import cn.yanque.modules.courses.pojo.entity.SysCourseDetailEntity;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Service
public class SysClassScheduleServiceImpl implements SysClassScheduleService {
    private static final String SCHEDULE_RULE_KEY = "teaching.schedule.rule";
    private static final int DAYS_PER_WEEK = 7;
    private static final int MAX_SCHEDULE_DAYS = 3660;

    private final SysClassMapper classMapper;
    private final SysClassScheduleMapper scheduleMapper;
    private final SysCourseDetailMapper courseDetailMapper;
    private final HolidayCalendarService holidayCalendarService;

    public SysClassScheduleServiceImpl(SysClassMapper classMapper,
                                       SysClassScheduleMapper scheduleMapper,
                                       SysCourseDetailMapper courseDetailMapper,
                                       HolidayCalendarService holidayCalendarService) {
        this.classMapper = classMapper;
        this.scheduleMapper = scheduleMapper;
        this.courseDetailMapper = courseDetailMapper;
        this.holidayCalendarService = holidayCalendarService;
    }

    @Override
    @Transactional
    public ClassScheduleGenerateRes generate(ClassScheduleGenerateReq req) {
        SysClassEntity clazz = getClassEntity(req.getClassId());
        TeachingScheduleRule rule = getAndValidateRule();
        Map<Integer, Map<LocalDate, String>> holidayCache = new java.util.HashMap<>();
        Map<LocalDate, String> configuredHolidays = parseHolidays(rule.getHolidays());

        int firstDayOfWeek = req.getFirstClassDate().getDayOfWeek().getValue();
        if ((Boolean.TRUE.equals(rule.getHolidayRest())
                && getHolidayName(req.getFirstClassDate(), holidayCache, configuredHolidays) != null)
                || !rule.getClassDays().contains(firstDayOfWeek)) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_FIRST_DATE_INVALID);
        }

        List<SysCourseDetailEntity> courseDetails = courseDetailMapper.selectByCourseId(clazz.getCourseId());
        if (CollUtil.isEmpty(courseDetails)) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_COURSE_DETAIL_EMPTY);
        }

        List<SysClassScheduleEntity> schedules = buildSchedules(clazz.getId(), req.getFirstClassDate(),
                courseDetails, rule, holidayCache, configuredHolidays);

        // 覆盖式生成：所有数据校验和内存排课成功后，才删除旧课表。
        scheduleMapper.deleteByClassId(clazz.getId());
        if (scheduleMapper.batchInsert(schedules) != schedules.size()) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_GENERATE_FAILED);
        }
        return new ClassScheduleGenerateRes(clazz.getId(), schedules.size());
    }

    @Override
    public List<ClassScheduleRes> list(Long classId, LocalDate startDate, LocalDate endDate) {
        getClassEntity(classId);
        if ((startDate == null) != (endDate == null)
                || (startDate != null && (startDate.isAfter(endDate)
                || startDate.plusYears(1).isBefore(endDate)))) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_DATE_RANGE_INVALID);
        }
        return scheduleMapper.selectByClassId(classId, startDate, endDate);
    }

    @Override
    public ClassScheduleDateDetailRes dateDetail(Long classId, LocalDate scheduleDate) {
        getClassEntity(classId);
        ClassScheduleDateDetailRes detail = scheduleMapper.selectDateDetail(classId, scheduleDate);
        if (detail == null) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_NOT_FOUND);
        }
        return detail;
    }

    @Override
    public List<ClassStageInfoRes> classStageInfo(Long classId) {
        getClassEntity(classId);
        List<ClassStageRangeRes> stageRanges = scheduleMapper.selectStageRanges(classId);
        List<TeacherOptionRes> teachers = scheduleMapper.selectActiveTeachers();

        List<ClassStageInfoRes> result = new ArrayList<>();
        for (ClassStageRangeRes stage : stageRanges) {
            Set<Long> busyTeacherIds = new HashSet<>(scheduleMapper.selectBusyTeacherIds(
                    classId, stage.getStartDate(), stage.getEndDate()));
            LinkedHashMap<Long, String> freeTeachers = teachers.stream()
                    .filter(teacher -> !busyTeacherIds.contains(teacher.getId()))
                    .collect(Collectors.toMap(TeacherOptionRes::getId, TeacherOptionRes::getTeacherName,
                            (left, right) -> left, LinkedHashMap::new));

            ClassStageInfoRes item = new ClassStageInfoRes();
            item.setStageName(stage.getStageName());
            item.setStageNumber(stage.getStageNumber());
            item.setFreeTeacherName(freeTeachers);
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public ClassScheduleTeacherAssignRes assignTeachers(Long classId, ClassScheduleTeacherAssignReq req) {
        getClassEntity(classId);
        List<ClassStageRangeRes> ranges = scheduleMapper.selectStageRanges(classId);
        if (ranges.isEmpty()) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_STAGE_NOT_FOUND);
        }
        Map<String, ClassStageRangeRes> rangeMap = ranges.stream().collect(Collectors.toMap(
                ClassStageRangeRes::getStageName, Function.identity()));
        Set<String> requestedStages = new HashSet<>();

        // 先校验所有阶段与老师，全部通过后再执行更新，避免只更新部分阶段。
        for (ClassScheduleTeacherAssignReq.StageTeacher assignment : req.getStages()) {
            String stageName = StrUtil.trim(assignment.getStageName());
            if (!requestedStages.add(stageName)) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_STAGE_REPEATED);
            }
            ClassStageRangeRes range = rangeMap.get(stageName);
            if (range == null) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_STAGE_NOT_FOUND);
            }
            if (scheduleMapper.countValidTeacher(assignment.getTeacherId()) == 0) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_TEACHER_INVALID);
            }
            if (scheduleMapper.countTeacherConflict(classId, assignment.getTeacherId(),
                    range.getStartDate(), range.getEndDate()) > 0) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_TEACHER_CONFLICT);
            }
        }

        int updateCount = 0;
        for (ClassScheduleTeacherAssignReq.StageTeacher assignment : req.getStages()) {
            updateCount += scheduleMapper.updateTeacherByStage(classId,
                    StrUtil.trim(assignment.getStageName()), assignment.getTeacherId());
        }
        return new ClassScheduleTeacherAssignRes(classId, updateCount);
    }

    @Override
    @Transactional
    public void addCourse(Long classId, ClassScheduleAddCourseReq req) {
        getClassEntity(classId);
        if (scheduleMapper.countValidTeacher(req.getTeacherId()) == 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_TEACHER_INVALID);
        }
        if (scheduleMapper.countTeacherConflict(classId, req.getTeacherId(),
                req.getScheduleDate(), req.getScheduleDate()) > 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_TEACHER_CONFLICT);
        }

        SysClassScheduleEntity selectedSchedule = scheduleMapper.selectByClassIdAndDate(
                classId, req.getScheduleDate());
        if (selectedSchedule == null
                || !ClassScheduleTypeEnum.CLASS.name().equals(selectedSchedule.getClassType())) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_INSERT_DATE_NOT_CLASS);
        }

        List<SysClassScheduleEntity> originalClasses = scheduleMapper.selectClassSchedulesFromDate(
                classId, req.getScheduleDate());
        TeachingScheduleRule rule = getAndValidateRule();
        Map<Integer, Map<LocalDate, String>> holidayCache = new java.util.HashMap<>();
        Map<LocalDate, String> configuredHolidays = parseHolidays(rule.getHolidays());

        SysClassScheduleEntity schedule = new SysClassScheduleEntity();
        schedule.setClassId(classId);
        schedule.setTeacherId(req.getTeacherId());
        schedule.setScheduleDate(req.getScheduleDate());
        schedule.setCourseDetailId(null);
        schedule.setCourseContent(StrUtil.trim(req.getCourseContent()));
        schedule.setClassType(ClassScheduleTypeEnum.CLASS.name());

        List<SysClassScheduleEntity> rebuiltSchedules = rebuildSchedulesAfterInsert(classId,
                req.getScheduleDate().plusDays(1), originalClasses, rule, holidayCache, configuredHolidays);

        // 所有节假日、排课规则及老师冲突校验完成后，再覆盖所选日期及后续课表。
        scheduleMapper.deleteSchedulesFromDate(classId, req.getScheduleDate());
        List<SysClassScheduleEntity> schedulesToInsert = new ArrayList<>();
        schedulesToInsert.add(schedule);
        schedulesToInsert.addAll(rebuiltSchedules);
        if (scheduleMapper.batchInsert(schedulesToInsert) != schedulesToInsert.size()) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_ADD_COURSE_FAILED);
        }
    }

    private List<SysClassScheduleEntity> rebuildSchedulesAfterInsert(
            Long classId,
            LocalDate startDate,
            List<SysClassScheduleEntity> originalClasses,
            TeachingScheduleRule rule,
            Map<Integer, Map<LocalDate, String>> holidayCache,
            Map<LocalDate, String> configuredHolidays) {
        List<SysClassScheduleEntity> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        int classIndex = 0;

        while (classIndex < originalClasses.size()) {
            if (result.size() >= MAX_SCHEDULE_DAYS) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
            }

            SysClassScheduleEntity next = new SysClassScheduleEntity();
            next.setClassId(classId);
            next.setScheduleDate(currentDate);
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            String holidayName = Boolean.TRUE.equals(rule.getHolidayRest())
                    ? getHolidayName(currentDate, holidayCache, configuredHolidays) : null;

            if (holidayName != null) {
                setNonClassSchedule(next, ClassScheduleTypeEnum.HOLIDAY, holidayName);
            } else if (rule.getRestDays().contains(dayOfWeek)) {
                setNonClassSchedule(next, ClassScheduleTypeEnum.REST, "休息日");
            } else if (rule.getSelfStudyDays().contains(dayOfWeek)) {
                setNonClassSchedule(next, ClassScheduleTypeEnum.SELF_STUDY, "自习日");
            } else if (rule.getClassDays().contains(dayOfWeek)) {
                SysClassScheduleEntity original = originalClasses.get(classIndex++);
                next.setTeacherId(original.getTeacherId());
                next.setCourseDetailId(original.getCourseDetailId());
                next.setCourseContent(original.getCourseContent());
                next.setClassType(ClassScheduleTypeEnum.CLASS.name());
                if (next.getTeacherId() != null && scheduleMapper.countTeacherConflict(classId,
                        next.getTeacherId(), currentDate, currentDate) > 0) {
                    throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_SHIFT_TEACHER_CONFLICT);
                }
            } else {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
            }
            result.add(next);
            currentDate = currentDate.plusDays(1);
        }
        return result;
    }

    @Override
    public List<TeacherScheduleItemRes> teacherSchedules(TeacherScheduleQueryReq req) {
        long rangeDays = ChronoUnit.DAYS.between(req.getStartTime(), req.getEndTime()) + 1;
        if (rangeDays < 1 || rangeDays > 45) {
            throw BusinessException.of(CommonErrorCode.TEACHER_SCHEDULE_DATE_RANGE_INVALID);
        }

        // 一次联表查询获得老师、班级和课表信息，再在内存中按老师分组，避免N+1查询。
        List<TeacherScheduleRowRes> rows = scheduleMapper.selectTeacherSchedules(
                req.getStartTime(), req.getEndTime());
        LinkedHashMap<Long, TeacherScheduleItemRes> teacherMap = new LinkedHashMap<>();
        for (TeacherScheduleRowRes row : rows) {
            TeacherScheduleItemRes teacher = teacherMap.computeIfAbsent(row.getTeacherId(), teacherId -> {
                TeacherScheduleItemRes item = new TeacherScheduleItemRes();
                item.setTeacherId(teacherId);
                item.setTeacherName(row.getTeacherName());
                return item;
            });

            TeacherScheduleDetailRes detail = new TeacherScheduleDetailRes();
            detail.setTeacheringDate(row.getScheduleDate());
            detail.setClassId(row.getClassId());
            detail.setClassPeriod(row.getClassPeriod());
            teacher.getTeacherDetailList().add(detail);
        }
        return new ArrayList<>(teacherMap.values());
    }

    private List<SysClassScheduleEntity> buildSchedules(Long classId,
                                                         LocalDate firstDate,
                                                         List<SysCourseDetailEntity> details,
                                                         TeachingScheduleRule rule,
                                                         Map<Integer, Map<LocalDate, String>> holidayCache,
                                                         Map<LocalDate, String> configuredHolidays) {
        List<SysClassScheduleEntity> schedules = new ArrayList<>();
        LocalDate currentDate = firstDate;
        int detailIndex = 0;

        while (detailIndex < details.size()) {
            if (schedules.size() >= MAX_SCHEDULE_DAYS) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
            }

            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            SysClassScheduleEntity schedule = new SysClassScheduleEntity();
            schedule.setClassId(classId);
            schedule.setScheduleDate(currentDate);

            String holidayName = Boolean.TRUE.equals(rule.getHolidayRest())
                    ? getHolidayName(currentDate, holidayCache, configuredHolidays) : null;
            if (holidayName != null) {
                setNonClassSchedule(schedule, ClassScheduleTypeEnum.HOLIDAY, holidayName);
            } else if (rule.getRestDays().contains(dayOfWeek)) {
                setNonClassSchedule(schedule, ClassScheduleTypeEnum.REST, "休息日");
            } else if (rule.getSelfStudyDays().contains(dayOfWeek)) {
                setNonClassSchedule(schedule, ClassScheduleTypeEnum.SELF_STUDY, "自习日");
            } else if (rule.getClassDays().contains(dayOfWeek)) {
                SysCourseDetailEntity detail = details.get(detailIndex++);
                schedule.setCourseDetailId(detail.getId());
                schedule.setCourseContent(StrUtil.blankToDefault(detail.getClassContent(), detail.getStageName()));
                schedule.setClassType(ClassScheduleTypeEnum.CLASS.name());
            } else {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
            }

            schedules.add(schedule);
            currentDate = currentDate.plusDays(1);
        }
        return schedules;
    }

    private void setNonClassSchedule(SysClassScheduleEntity schedule,
                                     ClassScheduleTypeEnum type,
                                     String content) {
        schedule.setCourseDetailId(null);
        schedule.setCourseContent(content);
        schedule.setClassType(type.name());
    }

    private TeachingScheduleRule getAndValidateRule() {
        String ruleJson = scheduleMapper.selectConfigValue(SCHEDULE_RULE_KEY);
        if (StrUtil.isBlank(ruleJson)) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_NOT_FOUND);
        }
        try {
            TeachingScheduleRule rule = JSON.parseObject(ruleJson, TeachingScheduleRule.class);
            if (rule == null) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
            }
            rule.setClassDays(defaultList(rule.getClassDays()));
            rule.setSelfStudyDays(defaultList(rule.getSelfStudyDays()));
            rule.setRestDays(defaultList(rule.getRestDays()));
            validateWeekDays(rule);
            return rule;
        } catch (JSONException exception) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
        }
    }

    private void validateWeekDays(TeachingScheduleRule rule) {
        if (rule.getClassDays().isEmpty()) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
        }
        Set<Integer> allDays = new HashSet<>();
        for (List<Integer> days : List.of(rule.getClassDays(), rule.getSelfStudyDays(), rule.getRestDays())) {
            for (Integer day : days) {
                if (day == null || day < 1 || day > DAYS_PER_WEEK || !allDays.add(day)) {
                    throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
                }
            }
        }
        if (allDays.size() != DAYS_PER_WEEK) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
        }
    }

    private Map<LocalDate, String> parseHolidays(List<String> holidayValues) {
        Map<LocalDate, String> holidays = new java.util.HashMap<>();
        if (holidayValues == null) {
            return holidays;
        }
        try {
            for (String value : holidayValues) {
                holidays.put(LocalDate.parse(value), "法定节假日");
            }
            return holidays;
        } catch (DateTimeException | NullPointerException exception) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_RULE_INVALID);
        }
    }

    private String getHolidayName(LocalDate date,
                                  Map<Integer, Map<LocalDate, String>> holidayCache,
                                  Map<LocalDate, String> configuredHolidays) {
        String configuredName = configuredHolidays.get(date);
        if (configuredName != null) {
            return configuredName;
        }
        Map<LocalDate, String> yearHolidays = holidayCache.computeIfAbsent(date.getYear(),
                holidayCalendarService::getHolidays);
        return yearHolidays.get(date);
    }

    private List<Integer> defaultList(List<Integer> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private SysClassEntity getClassEntity(Long classId) {
        SysClassEntity clazz = classMapper.selectById(classId);
        if (clazz == null) {
            throw BusinessException.of(CommonErrorCode.CLASS_NOT_FOUND);
        }
        return clazz;
    }
}
