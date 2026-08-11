package cn.yanque.modules.classes.mapper;

import cn.yanque.modules.classes.pojo.entity.SysClassScheduleEntity;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleDateDetailRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.time.LocalDate;

@Mapper
public interface SysClassScheduleMapper {
    String selectConfigValue(@Param("key") String key);
    List<ClassScheduleRes> selectByClassId(@Param("classId") Long classId,
                                           @Param("startDate") java.time.LocalDate startDate,
                                           @Param("endDate") java.time.LocalDate endDate);
    ClassScheduleDateDetailRes selectDateDetail(@Param("classId") Long classId,
                                                 @Param("scheduleDate") java.time.LocalDate scheduleDate);
    int deleteByClassId(@Param("classId") Long classId);
    int batchInsert(@Param("schedules") List<SysClassScheduleEntity> schedules);
    List<cn.yanque.modules.classes.pojo.vo.resvo.ClassStageRangeRes> selectStageRanges(
            @Param("classId") Long classId);
    List<cn.yanque.modules.classes.pojo.vo.resvo.TeacherOptionRes> selectActiveTeachers();
    List<Long> selectBusyTeacherIds(@Param("classId") Long classId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);
    int countValidTeacher(@Param("teacherId") Long teacherId);
    int countTeacherConflict(@Param("classId") Long classId,
                             @Param("teacherId") Long teacherId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);
    int updateTeacherByStage(@Param("classId") Long classId,
                             @Param("stageName") String stageName,
                             @Param("teacherId") Long teacherId);
    SysClassScheduleEntity selectByClassIdAndDate(@Param("classId") Long classId,
                                                   @Param("scheduleDate") LocalDate scheduleDate);
    int insertOne(SysClassScheduleEntity schedule);
    List<SysClassScheduleEntity> selectClassSchedulesFromDate(@Param("classId") Long classId,
                                                               @Param("fromDate") LocalDate fromDate);
    int deleteSchedulesFromDate(@Param("classId") Long classId,
                                @Param("fromDate") LocalDate fromDate);
    List<cn.yanque.modules.classes.pojo.vo.resvo.TeacherScheduleRowRes> selectTeacherSchedules(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
