package cn.yanque.modules.campuses.mapper;

import cn.yanque.modules.campuses.pojo.entity.SysCampusEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysCampusMapper {
    List<SysCampusEntity> selectPage(@Param("keyword") String keyword);
    SysCampusEntity selectById(@Param("id") Long id);
    int insert(SysCampusEntity campus);
    int updateById(SysCampusEntity campus);
    int deleteById(@Param("id") Long id);
}
