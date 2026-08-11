package cn.yanque.modules.studentexams.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.studentexams.pojo.vo.reqvo.StudentExamSubmitReq; import cn.yanque.modules.studentexams.pojo.vo.resvo.*; import cn.yanque.modules.studentexams.service.StudentExamService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*;
@Validated @RestController @RequestMapping("/student/exams")
public class StudentExamController {
    private final StudentExamService service;public StudentExamController(StudentExamService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<StudentExamRes>> list(@RequestParam(defaultValue="1") @Min(1) int pageNum,@RequestParam(defaultValue="10") @Min(1) @Max(100) int pageSize){return ApiResponse.success(service.myExams(pageNum,pageSize));}
    @PostMapping("/{id}/start") public ApiResponse<StudentExamStartRes> start(@PathVariable Long id){return ApiResponse.success(service.start(id));}
    @GetMapping("/records/{recordId}/paper") public ApiResponse<StudentExamPaperRes> paper(@PathVariable Long recordId){return ApiResponse.success(service.paper(recordId));}
    @PostMapping("/records/{recordId}/submit") public ApiResponse<StudentExamSubmitRes> submit(@PathVariable Long recordId,@Valid @RequestBody StudentExamSubmitReq req){return ApiResponse.success(service.submit(recordId,req));}
    @GetMapping("/records/{recordId}/submission") public ApiResponse<ExamSubmissionDetailRes> submission(@PathVariable Long recordId){return ApiResponse.success(service.submission(recordId));}
}
