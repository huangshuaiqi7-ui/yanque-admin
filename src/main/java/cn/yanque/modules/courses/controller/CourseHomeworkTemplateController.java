package cn.yanque.modules.courses.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseHomeworkTemplateSaveReq;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseHomeworkTemplateRes;
import cn.yanque.modules.courses.service.CourseHomeworkTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseHomeworkTemplateController {
    private final CourseHomeworkTemplateService templateService;

    public CourseHomeworkTemplateController(CourseHomeworkTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping("/{courseId}/homeworkTemplates")
    public ApiResponse<List<CourseHomeworkTemplateRes>> list(@PathVariable Long courseId) {
        return ApiResponse.success(templateService.list(courseId));
    }

    @GetMapping("/homeworkTemplates/{id}")
    public ApiResponse<CourseHomeworkTemplateRes> detail(@PathVariable Long id) {
        return ApiResponse.success(templateService.detail(id));
    }

    @PostMapping("/{courseId}/homeworkTemplates")
    public ApiResponse<Long> create(@PathVariable Long courseId,
                                    @Valid @RequestBody CourseHomeworkTemplateSaveReq req) {
        return ApiResponse.success(templateService.create(courseId, req));
    }

    @PutMapping("/homeworkTemplates/{id}")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @Valid @RequestBody CourseHomeworkTemplateSaveReq req) {
        templateService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/homeworkTemplates/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ApiResponse.success();
    }
}
