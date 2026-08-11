package cn.yanque.commons.dataconfig;

/**
 * @ClassName SysConfig
 * @Author mrzhang
 * @Date 2026/7/22
 * @Description 生产配置项
 *  数据库当中每行数据对应的一个对象, 目的就是添加 key对应的默认值.   只有数据库当中不存在的使用, 使用默认值.
 */
public class SysConfig {


    /**
     *  定义该对象的作用: 就是使用默认值.
     */
    public static final  SystemConfigItem<Long> JWTSECRET
             = new SystemConfigItem<>("jwt.secret", "123456", Long.class)  ;


    public static final  SystemConfigItem<Long> JWTEXPIRE
             = new SystemConfigItem<>("jwt.expire", "86400", Long.class)  ;
}
