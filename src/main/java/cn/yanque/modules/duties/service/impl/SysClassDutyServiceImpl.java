package cn.yanque.modules.duties.service.impl;

import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.enums.ClassDutyTypeEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.duties.mapper.SysClassDutyMapper;
import cn.yanque.modules.duties.pojo.entity.SysClassDutyEntity;
import cn.yanque.modules.duties.pojo.vo.reqvo.ClassDutyDateSaveReq;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateCampusRes;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateClassRes;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateRes;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutySaveRes;
import cn.yanque.modules.duties.service.SysClassDutyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysClassDutyServiceImpl implements SysClassDutyService {
    private final SysClassDutyMapper dutyMapper;

    public SysClassDutyServiceImpl(SysClassDutyMapper dutyMapper) {
        this.dutyMapper = dutyMapper;
    }

    @Override
    public ClassDutyDateRes dateDuty(LocalDate dutyDate) {
        List<ClassDutyDateClassRes> classDuties = dutyMapper.selectDateClasses(dutyDate);
        for (ClassDutyDateClassRes item : classDuties) {
            ClassDutyTypeEnum dutyType = "SELF_STUDY".equals(item.getClassType())
                    ? ClassDutyTypeEnum.SELF_STUDY_CLASS : ClassDutyTypeEnum.EVENING_STUDY_CLASS;
            item.setClassTypeDesc("SELF_STUDY".equals(item.getClassType()) ? "自习" : "上课");
            fillDutyInfo(item, dutyType);
        }

        List<ClassDutyDateCampusRes> campusDuties = dutyMapper.selectDateCampuses(dutyDate);
        for (ClassDutyDateCampusRes item : campusDuties) {
            fillDutyInfo(item, ClassDutyTypeEnum.EVENING_STUDY_CAMPUS);
        }

        ClassDutyDateRes result = new ClassDutyDateRes();
        result.setDutyDate(dutyDate);
        result.setClassDutyList(classDuties);
        result.setCampusDutyList(campusDuties);
        return result;
    }

    @Override
    @Transactional
    public ClassDutySaveRes saveDateDuty(ClassDutyDateSaveReq req) {
        Map<Long, ClassDutyDateClassRes> classCandidates = dutyMapper.selectDateClasses(req.getDutyDate())
                .stream().collect(Collectors.toMap(ClassDutyDateClassRes::getClassId, Function.identity()));
        Map<Long, ClassDutyDateCampusRes> campusCandidates = dutyMapper.selectDateCampuses(req.getDutyDate())
                .stream().collect(Collectors.toMap(ClassDutyDateCampusRes::getCampusId, Function.identity()));

        // 覆盖式保存。后续任一校验失败时，事务会恢复当天旧值班记录。
        dutyMapper.deleteByDutyDate(req.getDutyDate());

        List<SysClassDutyEntity> duties = new ArrayList<>();
        Set<String> classDutyKeys = new HashSet<>();
        Set<Long> campusDutyKeys = new HashSet<>();
        Set<Long> validatedTeachers = new HashSet<>();
        Map<Long, List<TimeRange>> teacherRanges = new HashMap<>();

        for (ClassDutyDateSaveReq.ClassDutyItem item : req.getClassDutyList()) {
            ClassDutyDateClassRes candidate = classCandidates.get(item.getClassId());
            if (candidate == null) {
                throw BusinessException.of(CommonErrorCode.CLASS_DUTY_CLASS_NOT_REQUIRED);
            }
            ClassDutyTypeEnum expectedType = "SELF_STUDY".equals(candidate.getClassType())
                    ? ClassDutyTypeEnum.SELF_STUDY_CLASS : ClassDutyTypeEnum.EVENING_STUDY_CLASS;
            if (!expectedType.name().equals(item.getDutyType())) {
                throw BusinessException.of(CommonErrorCode.CLASS_DUTY_TYPE_INVALID);
            }
            String duplicateKey = item.getClassId() + ":" + item.getDutyType();
            if (!classDutyKeys.add(duplicateKey)) {
                throw BusinessException.of(CommonErrorCode.CLASS_DUTY_DUPLICATED);
            }
            validateTeacher(item.getTeacherId(), validatedTeachers);
            validateTimeConflict(item.getTeacherId(), expectedType, teacherRanges);
            duties.add(buildDuty(item.getClassId(), candidate.getCampusId(), item.getTeacherId(),
                    req.getDutyDate(), expectedType));
        }

        for (ClassDutyDateSaveReq.CampusDutyItem item : req.getCampusDutyList()) {
            if (!ClassDutyTypeEnum.EVENING_STUDY_CAMPUS.name().equals(item.getDutyType())) {
                throw BusinessException.of(CommonErrorCode.CLASS_DUTY_TYPE_INVALID);
            }
            if (!campusCandidates.containsKey(item.getCampusId())
                    || dutyMapper.countCampusCandidate(item.getCampusId(), req.getDutyDate()) == 0) {
                throw BusinessException.of(CommonErrorCode.CLASS_DUTY_CAMPUS_NOT_REQUIRED);
            }
            if (!campusDutyKeys.add(item.getCampusId())) {
                throw BusinessException.of(CommonErrorCode.CLASS_DUTY_CAMPUS_DUPLICATED);
            }
            validateTeacher(item.getTeacherId(), validatedTeachers);
            validateTimeConflict(item.getTeacherId(), ClassDutyTypeEnum.EVENING_STUDY_CAMPUS, teacherRanges);
            duties.add(buildDuty(null, item.getCampusId(), item.getTeacherId(), req.getDutyDate(),
                    ClassDutyTypeEnum.EVENING_STUDY_CAMPUS));
        }

        if (!duties.isEmpty() && dutyMapper.batchInsert(duties) != duties.size()) {
            throw BusinessException.of(CommonErrorCode.CLASS_DUTY_SAVE_FAILED);
        }
        return new ClassDutySaveRes(duties.size());
    }

    private void validateTeacher(Long teacherId, Set<Long> validatedTeachers) {
        if (validatedTeachers.add(teacherId) && dutyMapper.countValidTeacher(teacherId) == 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_DUTY_TEACHER_INVALID);
        }
    }

    private void validateTimeConflict(Long teacherId, ClassDutyTypeEnum dutyType,
                                      Map<Long, List<TimeRange>> teacherRanges) {
        LocalTime start = LocalTime.parse(dutyType.getStartTime());
        LocalTime end = LocalTime.parse(dutyType.getEndTime());
        List<TimeRange> ranges = teacherRanges.computeIfAbsent(teacherId, key -> new ArrayList<>());
        boolean overlapped = ranges.stream().anyMatch(range -> start.isBefore(range.end())
                && end.isAfter(range.start()));
        if (overlapped) {
            throw BusinessException.of(CommonErrorCode.CLASS_DUTY_TEACHER_CONFLICT);
        }
        ranges.add(new TimeRange(start, end));
    }

    private SysClassDutyEntity buildDuty(Long classId, Long campusId, Long teacherId,
                                          LocalDate dutyDate, ClassDutyTypeEnum dutyType) {
        SysClassDutyEntity duty = new SysClassDutyEntity();
        duty.setClassId(classId);
        duty.setCampusId(campusId);
        duty.setTeacherId(teacherId);
        duty.setDutyDate(dutyDate);
        duty.setDutyType(dutyType.name());
        duty.setStartTime(dutyType.getStartTime());
        duty.setEndTime(dutyType.getEndTime());
        return duty;
    }

    private void fillDutyInfo(ClassDutyDateClassRes item, ClassDutyTypeEnum dutyType) {
        item.setDutyType(dutyType.name());
        item.setDutyTypeDesc(dutyType.getDescription());
        item.setStartTime(dutyType.getStartTime());
        item.setEndTime(dutyType.getEndTime());
    }

    private void fillDutyInfo(ClassDutyDateCampusRes item, ClassDutyTypeEnum dutyType) {
        item.setDutyType(dutyType.name());
        item.setDutyTypeDesc(dutyType.getDescription());
        item.setStartTime(dutyType.getStartTime());
        item.setEndTime(dutyType.getEndTime());
    }

    private record TimeRange(LocalTime start, LocalTime end) {
    }
}
