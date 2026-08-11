package cn.yanque.commons.dataconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

/**
 * @ClassName SystemConfigItem
 * @Author mrzhang
 * @Date 2026/7/22
 * @Description sys_config 这个表 配置项定义类
 */
@Data
@AllArgsConstructor
public class SystemConfigItem<T> {

    /*
    * 配置项key
     */
    private String key;

    /*
    * 配置项value
     */

    private String defaultValue;


    /*
    * 配置项要转换成的数据类型
     */

    private Class<T> clazz;




}
