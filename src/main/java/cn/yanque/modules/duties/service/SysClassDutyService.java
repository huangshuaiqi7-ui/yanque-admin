package cn.yanque.modules.duties.service;

import cn.yanque.modules.duties.pojo.vo.reqvo.ClassDutyDateSaveReq;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateRes;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutySaveRes;
import java.time.LocalDate;

public interface SysClassDutyService {
    ClassDutyDateRes dateDuty(LocalDate dutyDate);
    ClassDutySaveRes saveDateDuty(ClassDutyDateSaveReq req);
}
