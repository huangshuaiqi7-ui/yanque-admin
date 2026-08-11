package cn.yanque.modules.courses.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseDetailSaveReq;
import cn.yanque.modules.courses.pojo.vo.reqvo.CoursePageReq;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseSaveReq;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseDetailRes;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseRes;
import cn.yanque.modules.courses.service.SysCourseService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class SysCourseController {
    private final SysCourseService courseService;

    public SysCourseController(SysCourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<PageResult<CourseRes>> page(@Valid CoursePageReq req) {
        return ApiResponse.success(courseService.page(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseRes> detail(@PathVariable Long id) {
        return ApiResponse.success(courseService.detail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CourseSaveReq req) {
        return ApiResponse.success(courseService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CourseSaveReq req) {
        courseService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{courseId}/details")
    public ApiResponse<List<CourseDetailRes>> listDetails(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.listDetails(courseId));
    }

    @GetMapping("/{courseId}/stages")
    public ApiResponse<List<String>> listStages(@PathVariable Long courseId) {
        return ApiResponse.success(courseService.listStages(courseId));
    }

    @GetMapping("/details/{id}")
    public ApiResponse<CourseDetailRes> detailItem(@PathVariable Long id) {
        return ApiResponse.success(courseService.detailItem(id));
    }

    @PostMapping("/{courseId}/details")
    public ApiResponse<Long> createDetail(@PathVariable Long courseId,
                                          @Valid @RequestBody CourseDetailSaveReq req) {
        return ApiResponse.success(courseService.createDetail(courseId, req));
    }

    @PutMapping("/details/{id}")
    public ApiResponse<Void> updateDetail(@PathVariable Long id,
                                          @Valid @RequestBody CourseDetailSaveReq req) {
        courseService.updateDetail(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/details/{id}")
    public ApiResponse<Void> deleteDetail(@PathVariable Long id) {
        courseService.deleteDetail(id);
        return ApiResponse.success();
    }

    @PostMapping(value = "/{courseId}/details/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> importDetails(@PathVariable Long courseId,
                                           @RequestPart("file") MultipartFile file) {
        courseService.importDetails(courseId, file);
        return ApiResponse.success();
    }
}
