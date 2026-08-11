package cn.yanque.modules.configs.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.configs.pojo.vo.reqvo.SysConfigPageReq;
import cn.yanque.modules.configs.pojo.vo.reqvo.SysConfigSaveReq;
import cn.yanque.modules.configs.pojo.vo.resvo.SysConfigRes;
import cn.yanque.modules.configs.service.SysConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sysConfig")
public class SysConfigController {
    private final SysConfigService configService;

    public SysConfigController(SysConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ApiResponse<PageResult<SysConfigRes>> page(@Valid SysConfigPageReq req) {
        return ApiResponse.success(configService.page(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysConfigRes> detail(@PathVariable Long id) {
        return ApiResponse.success(configService.detail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody SysConfigSaveReq req) {
        return ApiResponse.success(configService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody SysConfigSaveReq req) {
        configService.update(id, req);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return ApiResponse.success();
    }
}
