package net.xdclass.dao;

import net.xdclass.domain.Video;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @Description 旭瑶&小滴课堂 xdclass.net
 * @Author 二当家小D  代码、笔记和技术指导联系我即可
 * @Version 1.0
 **/

@Repository
public interface VideoMapper {

    @Select("select * from video where id=#{videoId}")
    Video findById(@Param("videoId") int videoId);


}
