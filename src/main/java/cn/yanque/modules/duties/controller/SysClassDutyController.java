package cn.yanque.modules.duties.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.duties.pojo.vo.reqvo.ClassDutyDateSaveReq;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateRes;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutySaveRes;
import cn.yanque.modules.duties.service.SysClassDutyService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/classDuties")
public class SysClassDutyController {
    private final SysClassDutyService dutyService;

    public SysClassDutyController(SysClassDutyService dutyService) {
        this.dutyService = dutyService;
    }

    @GetMapping("/date")
    public ApiResponse<ClassDutyDateRes> dateDuty(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dutyDate) {
        return ApiResponse.success(dutyService.dateDuty(dutyDate));
    }

    @PutMapping("/date")
    public ApiResponse<ClassDutySaveRes> saveDateDuty(@Valid @RequestBody ClassDutyDateSaveReq req) {
        return ApiResponse.success(dutyService.saveDateDuty(req));
    }
}
