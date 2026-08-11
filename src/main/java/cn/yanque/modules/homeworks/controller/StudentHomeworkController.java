package cn.yanque.modules.homeworks.controller;
import cn.yanque.commons.apires.*; import cn.yanque.commons.pojo.vo.reqvo.PresignUploadReq; import cn.yanque.commons.pojo.vo.resvo.*;
import cn.yanque.modules.homeworks.pojo.vo.reqvo.*; import cn.yanque.modules.homeworks.pojo.vo.resvo.*; import cn.yanque.modules.homeworks.service.StudentHomeworkService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*;

@Validated @RestController @RequestMapping("/student")
public class StudentHomeworkController {
    private final StudentHomeworkService service; public StudentHomeworkController(StudentHomeworkService service){this.service=service;}
    @PostMapping("/login") public ApiResponse<StudentLoginRes> login(@Valid @RequestBody StudentLoginReq req){return ApiResponse.success(service.login(req));}
    @GetMapping("/homeworks") public ApiResponse<PageResult<StudentHomeworkRes>> page(@RequestParam(defaultValue="1") @Min(1) int pageNum,@RequestParam(defaultValue="10") @Min(1) @Max(100) int pageSize){return ApiResponse.success(service.page(pageNum,pageSize));}
    @GetMapping("/homeworks/{id}/download-url") public ApiResponse<PresignDownloadRes> download(@PathVariable Long id,@RequestParam String type){return ApiResponse.success(service.homeworkDownload(id,type));}
    @PostMapping("/homeworks/{id}/submissions") public ApiResponse<StudentSubmissionRes> submit(@PathVariable Long id,@Valid @RequestBody StudentSubmissionReq req){return ApiResponse.success(service.submit(id,req));}
    @GetMapping("/homeworks/{id}/submissions/download-url") public ApiResponse<PresignDownloadRes> submissionDownload(@PathVariable Long id){return ApiResponse.success(service.submissionDownload(id));}
    @PostMapping("/upload/presign-upload") public ApiResponse<PresignUploadRes> presign(@Valid @RequestBody PresignUploadReq req){return ApiResponse.success(service.presignSubmission(req.getObjectKey()));}
}
