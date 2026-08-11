package cn.yanque.modules.studentexams.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.studentexams.pojo.vo.reqvo.ExamGradeReq; import cn.yanque.modules.studentexams.pojo.vo.resvo.*; import cn.yanque.modules.studentexams.service.AdminExamSubmissionService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*; import java.util.Map;
@Validated @RestController @RequestMapping("/api/exams")
public class AdminExamSubmissionController {
    private final AdminExamSubmissionService service;public AdminExamSubmissionController(AdminExamSubmissionService service){this.service=service;}
    @GetMapping("/{id}/submissions") public ApiResponse<PageResult<ExamSubmissionListRes>> page(@PathVariable Long id,@RequestParam(defaultValue="1") @Min(1) int pageNum,@RequestParam(defaultValue="10") @Min(1) @Max(1000) int pageSize){return ApiResponse.success(service.page(id,pageNum,pageSize));}
    @GetMapping("/submissions/{recordId}") public ApiResponse<ExamSubmissionDetailRes> detail(@PathVariable Long recordId){return ApiResponse.success(service.detail(recordId));}
    @PutMapping("/submissions/{recordId}/grade") public ApiResponse<Map<String,Object>> grade(@PathVariable Long recordId,@Valid @RequestBody ExamGradeReq req){ExamSubmissionDetailRes res=service.grade(recordId,req);return ApiResponse.success(Map.of("recordId",recordId,"score",res.getScore(),"gradingStatus",res.getGradingStatus()));}
}
