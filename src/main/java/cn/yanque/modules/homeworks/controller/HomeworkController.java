package cn.yanque.modules.homeworks.controller;
import cn.yanque.commons.apires.*;
import cn.yanque.modules.homeworks.pojo.vo.reqvo.*;
import cn.yanque.modules.homeworks.pojo.vo.resvo.*;
import cn.yanque.modules.homeworks.service.HomeworkService;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Validated @RestController @RequestMapping("/api/homeworks")
public class HomeworkController {
    private final HomeworkService service;
    public HomeworkController(HomeworkService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<HomeworkRes>> page(@Valid HomeworkPageReq req){return ApiResponse.success(service.page(req));}
    @GetMapping("/prepare") public ApiResponse<HomeworkPrepareRes> prepare(@RequestParam @Positive Long classId,@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate homeworkDate){return ApiResponse.success(service.prepare(classId,homeworkDate));}
    @PostMapping public ApiResponse<Long> create(@Valid @RequestBody HomeworkCreateReq req){return ApiResponse.success(service.create(req));}
    @PutMapping("/{id}/answer") public ApiResponse<Void> answer(@PathVariable Long id,@Valid @RequestBody HomeworkAnswerReq req){service.publishAnswer(id,req);return ApiResponse.success();}
    @GetMapping("/{id}/submissions") public ApiResponse<PageResult<HomeworkSubmissionRes>> submissions(@PathVariable Long id,@RequestParam(defaultValue="1") @Min(1) int pageNum,@RequestParam(defaultValue="10") @Min(1) @Max(1000) int pageSize){return ApiResponse.success(service.submissions(id,pageNum,pageSize));}
    @PutMapping("/submissions/{id}/grade") public ApiResponse<Void> grade(@PathVariable Long id,@Valid @RequestBody HomeworkGradeReq req){service.grade(id,req);return ApiResponse.success();}
}
