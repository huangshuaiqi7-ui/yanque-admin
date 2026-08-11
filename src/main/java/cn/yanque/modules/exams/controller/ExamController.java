package cn.yanque.modules.exams.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.exams.pojo.vo.reqvo.*; import cn.yanque.modules.exams.pojo.vo.resvo.ExamRes; import cn.yanque.modules.exams.service.ExamService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/exams")
public class ExamController {
    private final ExamService service;public ExamController(ExamService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<ExamRes>> page(@Valid ExamPageReq req){return ApiResponse.success(service.page(req));}
    @GetMapping("/{id}") public ApiResponse<ExamRes> detail(@PathVariable Long id){return ApiResponse.success(service.detail(id));}
    @PostMapping public ApiResponse<Map<String,Long>> create(@Valid @RequestBody ExamSaveReq req){return ApiResponse.success(Map.of("id",service.create(req)));}
    @PutMapping("/{id}") public ApiResponse<Map<String,Long>> update(@PathVariable Long id,@Valid @RequestBody ExamSaveReq req){service.update(id,req);return ApiResponse.success(Map.of("id",id));}
    @DeleteMapping("/{id}") public ApiResponse<Map<String,Long>> delete(@PathVariable Long id){service.delete(id);return ApiResponse.success(Map.of("id",id));}
    @PutMapping("/{id}/answer-visible") public ApiResponse<Map<String,Object>> visible(@PathVariable Long id,@Valid @RequestBody ExamAnswerVisibleReq req){service.updateAnswerVisible(id,req);return ApiResponse.success(Map.of("id",id,"answerVisible",req.getAnswerVisible()));}
}
