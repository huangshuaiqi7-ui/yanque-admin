package cn.yanque.modules.classes.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.exception.BusinessException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class HolidayCalendarService {
    private static final int REQUEST_TIMEOUT_MILLIS = 5000;

    private final String holidayApiUrl;

    public HolidayCalendarService(
            @Value("${teaching.holiday-api-url:https://timor.tech/api/holiday/year/}") String holidayApiUrl) {
        this.holidayApiUrl = holidayApiUrl;
    }

    /**
     * 查询指定年份的法定放假日期。接口中 holiday=false 的数据是调休补班日，不计为节假日。
     */
    public Map<LocalDate, String> getHolidays(int year) {
        try (HttpResponse response = HttpRequest.get(holidayApiUrl + year)
                .timeout(REQUEST_TIMEOUT_MILLIS)
                .execute()) {
            if (!response.isOk()) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_HOLIDAY_QUERY_FAILED);
            }
            JSONObject result = JSON.parseObject(response.body());
            if (result == null || result.getIntValue("code") != 0 || result.getJSONObject("holiday") == null) {
                throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_HOLIDAY_QUERY_FAILED);
            }

            Map<LocalDate, String> holidays = new HashMap<>();
            for (Object value : result.getJSONObject("holiday").values()) {
                JSONObject holiday = (JSONObject) value;
                if (holiday.getBooleanValue("holiday")) {
                    holidays.put(LocalDate.parse(holiday.getString("date")), holiday.getString("name"));
                }
            }
            return holidays;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.CLASS_SCHEDULE_HOLIDAY_QUERY_FAILED);
        }
    }
}
