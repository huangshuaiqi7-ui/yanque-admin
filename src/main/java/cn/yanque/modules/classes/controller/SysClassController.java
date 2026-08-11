package cn.yanque.modules.classes.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassPageReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassSaveReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleGenerateReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleTeacherAssignReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassScheduleAddCourseReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.TeacherScheduleQueryReq;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleGenerateRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleDateDetailRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassScheduleTeacherAssignRes;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassStageInfoRes;
import cn.yanque.modules.classes.pojo.vo.resvo.TeacherScheduleItemRes;
import cn.yanque.modules.classes.service.SysClassService;
import cn.yanque.modules.classes.service.SysClassScheduleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/classes")
public class SysClassController {
    private final SysClassService classService;
    private final SysClassScheduleService classScheduleService;

    public SysClassController(SysClassService classService, SysClassScheduleService classScheduleService) {
        this.classService = classService;
        this.classScheduleService = classScheduleService;
    }

    @GetMapping
    public ApiResponse<PageResult<ClassRes>> page(@Valid ClassPageReq req) {
        return ApiResponse.success(classService.page(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClassRes> detail(@PathVariable Long id) {
        return ApiResponse.success(classService.detail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ClassSaveReq req) {
        return ApiResponse.success(classService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody ClassSaveReq req) {
        classService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/schedules/generate")
    public ApiResponse<ClassScheduleGenerateRes> generateSchedule(
            @Valid @RequestBody ClassScheduleGenerateReq req) {
        return ApiResponse.success(classScheduleService.generate(req));
    }

    @GetMapping("/schedules/{classId}")
    public ApiResponse<java.util.List<ClassScheduleRes>> listSchedules(
            @PathVariable Long classId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(classScheduleService.list(classId, startDate, endDate));
    }

    @GetMapping("/schedules/{classId}/date-detail")
    public ApiResponse<ClassScheduleDateDetailRes> dateDetail(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate scheduleDate) {
        return ApiResponse.success(classScheduleService.dateDetail(classId, scheduleDate));
    }

    @GetMapping("/schedules/{classId}/classStageInfo")
    public ApiResponse<java.util.List<ClassStageInfoRes>> classStageInfo(@PathVariable Long classId) {
        return ApiResponse.success(classScheduleService.classStageInfo(classId));
    }

    @PutMapping("/schedules/{classId}/teachers")
    public ApiResponse<ClassScheduleTeacherAssignRes> assignTeachers(
            @PathVariable Long classId,
            @Valid @RequestBody ClassScheduleTeacherAssignReq req) {
        return ApiResponse.success(classScheduleService.assignTeachers(classId, req));
    }

    @PutMapping({"/schedules/{classId}/addClassSchule", "/schedules/{classId}/addClassSchedule"})
    public ApiResponse<Void> addCourse(@PathVariable Long classId,
                                       @Valid @RequestBody ClassScheduleAddCourseReq req) {
        classScheduleService.addCourse(classId, req);
        return ApiResponse.success();
    }

    @PostMapping("/schedules/teacher-detail")
    public ApiResponse<java.util.List<TeacherScheduleItemRes>> teacherSchedules(
            @Valid @RequestBody TeacherScheduleQueryReq req) {
        return ApiResponse.success(classScheduleService.teacherSchedules(req));
    }
}
