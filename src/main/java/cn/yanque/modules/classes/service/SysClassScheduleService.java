package cn.yanque.modules.classes.service;

import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleGenerateReq;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleGenerateRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleRes;
import java.util.List;
import java.time.LocalDate;

public interface SysClassScheduleService {
    ClassScheduleGenerateRes generate(ClassScheduleGenerateReq req);
    List<ClassScheduleRes> list(Long classId, LocalDate startDate, LocalDate endDate);
    cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleDateDetailRes dateDetail(Long classId,
                                                                                  LocalDate scheduleDate);
    List<cn.yanque.modules.classes.pojo.vo.resvo.ClassStageInfoRes> classStageInfo(Long classId);
    cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleTeacherAssignRes assignTeachers(
            Long classId,
            cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleTeacherAssignReq req);
    void addCourse(Long classId,
                   cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleAddCourseReq req);
    List<cn.yanque.modules.classes.pojo.vo.resvo.TeacherScheduleItemRes> teacherSchedules(
            cn.yanque.modules.classes.pojo.vo.reqvo.TeacherScheduleQueryReq req);
}
