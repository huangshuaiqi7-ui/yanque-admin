package cn.yanque.commons.dataconfig;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjUtil;
import cn.yanque.modules.configs.mapper.SysConfigMapper;
import cn.yanque.modules.configs.pojo.entity.SysConfigEntity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName SystemConfigService
 * @Author mrzhang
 * @Date 2026/7/22
 * @Description 查询sys_config 表的服务类.
 */
@Component
public class SystemConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;


    //1:创建一个本地缓存对象.
    private final  Cache<String, Object> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)//缓存对象在10秒内没有被访问过, 就会过期.
            .maximumSize(1000)//最多缓存1000个对象.
            .build();


    /*
        查询过程:
        (1) 线程本地缓存.
        (2) 查询MySQL查询.
        (3) 使用默认值 .
     */

    public <T> T get(SystemConfigItem<T>  item) {
        // 1: 本地缓存当中获取.
        Object cacheValue = cache.getIfPresent(item.getKey());

        if (ObjUtil.isNotEmpty(cacheValue)){
            // 用户了hutool工具包,进行了数据类型的转换.
            return Convert.convert(item.getClazz(), cacheValue);
        }
        // 2: 从MySQL数据库当中获取.
        SysConfigEntity sysConfigEntity = sysConfigMapper.selectByKey(item.getKey());
        if (ObjUtil.isNotEmpty(sysConfigEntity)){
            // 3: 本地缓存对象.
            cache.put(item.getKey(), sysConfigEntity.getV());
            // 4: 返回结果.
            return Convert.convert(item.getClazz(), sysConfigEntity.getV());
        }

        // 3: MySQL当中没查询到数据, 此时需要使用默认值.
        cache.put(item.getKey(), item.getDefaultValue());
        return Convert.convert(item.getClazz(), item.getDefaultValue());

    }
}
