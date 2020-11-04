package com.zr.video.controller;

import com.zr.video.entity.vo.VideoVo;
import com.zr.video.service.VideoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:04
 */
@RestController
@RequestMapping("video")
@Api("视频")
@Slf4j
public class VideoController {
    @Resource
    private VideoService videoService;

    @GetMapping
    public String hello(HttpServletRequest request) {
        return "hello";
    }

    @GetMapping("getById")
    public VideoVo getById(@RequestParam String id, HttpServletRequest request) {
        log.info("请求地址 {} 端口：{}", request.getServerName(), request.getServerPort());
        VideoVo vo = videoService.getById(id);
        vo.setServerAddr(request.getRequestURL().toString());
        return vo;
    }
}
