package cn.yanque.modules.courses.pojo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 课程详情 Excel 导入行，只读取模板前三列。
 */
@Data
public class CourseDetailImportRow {
    @ExcelProperty(value = "阶段名称", index = 0)
    private String stageName;

    @ExcelProperty(value = "第几天", index = 1)
    private Integer dayNumber;

    @ExcelProperty(value = "上课内容", index = 2)
    private String classContent;
}
