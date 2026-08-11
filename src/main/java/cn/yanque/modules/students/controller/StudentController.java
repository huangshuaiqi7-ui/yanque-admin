package cn.yanque.modules.students.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentClassAssignReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentPageReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentSopAssignReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentTagUpdateReq;
import cn.yanque.modules.students.pojo.vo.resvo.StudentRes;
import cn.yanque.modules.students.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<PageResult<StudentRes>> page(@Valid StudentPageReq req) {
        return ApiResponse.success(studentService.page(req));
    }

    @PutMapping("/{id}/class")
    public ApiResponse<Map<String, Long>> assignClass(@PathVariable Long id, @Valid @RequestBody StudentClassAssignReq req) {
        studentService.assignClass(id, req);
        return ApiResponse.success(Map.of("studentId", id, "classId", req.getClassId()));
    }

    @GetMapping("/tag-options")
    public ApiResponse<List<String>> tagOptions() {
        return ApiResponse.success(studentService.tagOptions());
    }

    @PutMapping("/{id}/tag")
    public ApiResponse<Map<String, Object>> updateTag(@PathVariable Long id, @Valid @RequestBody StudentTagUpdateReq req) {
        studentService.updateTag(id, req);
        return ApiResponse.success(Map.of("studentId", id, "studentTag", req.getStudentTag() == null ? "" : req.getStudentTag()));
    }

    @PostMapping("/{id}/sop")
    public ApiResponse<Map<String, Long>> assignSop(@PathVariable Long id, @Valid @RequestBody StudentSopAssignReq req) {
        Long sopId = studentService.assignSop(id, req);
        return ApiResponse.success(Map.of("id", sopId, "studentId", id));
    }
}
