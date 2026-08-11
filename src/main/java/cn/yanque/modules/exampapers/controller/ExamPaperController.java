package cn.yanque.modules.exampapers.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.exampapers.pojo.vo.reqvo.*; import cn.yanque.modules.exampapers.pojo.vo.resvo.ExamPaperRes; import cn.yanque.modules.exampapers.service.ExamPaperService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/examPapers")
public class ExamPaperController {
    private final ExamPaperService service;public ExamPaperController(ExamPaperService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<ExamPaperRes>> page(@Valid ExamPaperPageReq req){return ApiResponse.success(service.page(req));}
    @GetMapping("/{id}") public ApiResponse<ExamPaperRes> detail(@PathVariable Long id){return ApiResponse.success(service.detail(id));}
    @PostMapping public ApiResponse<Map<String,Long>> create(@Valid @RequestBody ExamPaperSaveReq req){return ApiResponse.success(Map.of("id",service.create(req)));}
    @DeleteMapping("/{id}") public ApiResponse<Map<String,Long>> delete(@PathVariable Long id){service.delete(id);return ApiResponse.success(Map.of("id",id));}
}
