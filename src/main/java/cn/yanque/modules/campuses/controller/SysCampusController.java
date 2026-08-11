package cn.yanque.modules.campuses.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.campuses.pojo.vo.reqvo.CampusPageReq;
import cn.yanque.modules.campuses.pojo.vo.reqvo.CampusSaveReq;
import cn.yanque.modules.campuses.pojo.vo.resvo.CampusRes;
import cn.yanque.modules.campuses.service.SysCampusService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campus")
public class SysCampusController {
    private final SysCampusService campusService;

    public SysCampusController(SysCampusService campusService) {
        this.campusService = campusService;
    }

    @GetMapping
    public ApiResponse<PageResult<CampusRes>> page(@Valid CampusPageReq req) {
        return ApiResponse.success(campusService.page(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<CampusRes> detail(@PathVariable Long id) {
        return ApiResponse.success(campusService.detail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CampusSaveReq req) {
        return ApiResponse.success(campusService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CampusSaveReq req) {
        campusService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        campusService.delete(id);
        return ApiResponse.success();
    }
}
