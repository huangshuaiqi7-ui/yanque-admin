package cn.yanque.commons.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.pojo.vo.reqvo.PresignUploadReq;
import cn.yanque.commons.pojo.vo.resvo.PresignDownloadRes;
import cn.yanque.commons.pojo.vo.resvo.PresignUploadRes;
import cn.yanque.commons.service.TosPresignService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/upload")
@Validated
public class UploadController {
    private final TosPresignService tosPresignService;

    public UploadController(TosPresignService tosPresignService) {
        this.tosPresignService = tosPresignService;
    }

    @PostMapping("/presign-upload")
    public ApiResponse<PresignUploadRes> presignUpload(@Valid @RequestBody PresignUploadReq req) {
        return ApiResponse.success(tosPresignService.presignUpload(req.getObjectKey()));
    }

    @GetMapping("/presign-download")
    public ApiResponse<PresignDownloadRes> presignDownload(
            @RequestParam @NotBlank(message = "对象Key不能为空") String objectKey) {
        return ApiResponse.success(tosPresignService.presignDownload(objectKey));
    }
}
