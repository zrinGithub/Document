package com.zr.video.dao;

import com.zr.video.dao.po.VideoDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:07
 */
@Mapper
public interface VideoMapper {
    @Select("select * from video where id=#{id}")
    VideoDo getById(@Param("id") String id);
}
