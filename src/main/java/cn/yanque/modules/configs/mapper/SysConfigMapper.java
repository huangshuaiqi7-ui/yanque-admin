package cn.yanque.modules.configs.mapper;

import cn.yanque.modules.configs.pojo.entity.SysConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysConfigMapper {
    List<SysConfigEntity> selectPage(@Param("keyword") String keyword);
    SysConfigEntity selectById(@Param("id") Long id);
    SysConfigEntity selectByKey(@Param("key") String key);
    int insert(SysConfigEntity config);
    int updateById(SysConfigEntity config);
    int deleteById(@Param("id") Long id);
}
