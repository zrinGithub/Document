package com.zr.order.service;

import com.zr.video.entity.vo.VideoVo;
import com.zr.order.service.fallback.VideoServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/26 11:26
 */
@FeignClient(value = "video-service", fallback = VideoServiceFallBack.class)
//@RequestMapping("video")如果注解加在这里，会和VideoServiceFallBack报重复实现的错误https://github.com/spring-cloud/spring-cloud-netflix/issues/2677
public interface VideoService {
    @GetMapping("video/getById")
    VideoVo getById(@RequestParam("id") String id);
}
