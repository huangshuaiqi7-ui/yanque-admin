package cn.yanque.modules.examquestions.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.examquestions.pojo.vo.reqvo.*; import cn.yanque.modules.examquestions.pojo.vo.resvo.ExamQuestionRes; import cn.yanque.modules.examquestions.service.ExamQuestionService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/examQuestions")
public class ExamQuestionController {
    private final ExamQuestionService service; public ExamQuestionController(ExamQuestionService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<ExamQuestionRes>> page(@Valid ExamQuestionPageReq req){return ApiResponse.success(service.page(req));}
    @GetMapping("/{id}") public ApiResponse<ExamQuestionRes> detail(@PathVariable Long id){return ApiResponse.success(service.detail(id));}
    @PostMapping public ApiResponse<Map<String,Long>> create(@Valid @RequestBody ExamQuestionSaveReq req){return ApiResponse.success(Map.of("id",service.create(req)));}
    @PutMapping("/{id}") public ApiResponse<Map<String,Long>> update(@PathVariable Long id,@Valid @RequestBody ExamQuestionSaveReq req){service.update(id,req);return ApiResponse.success(Map.of("id",id));}
    @DeleteMapping("/{id}") public ApiResponse<Map<String,Long>> delete(@PathVariable Long id){service.delete(id);return ApiResponse.success(Map.of("id",id));}
    @PutMapping("/{id}/status") public ApiResponse<Map<String,Object>> status(@PathVariable Long id,@Valid @RequestBody ExamQuestionStatusReq req){service.updateStatus(id,req);return ApiResponse.success(Map.of("id",id,"status",req.getStatus().toUpperCase()));}
}
