package com.zr.order.controller;

import com.zr.order.service.VideoService;
import com.zr.video.entity.vo.VideoVo;
import com.zr.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:32
 */
@RestController
@RequestMapping("order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private VideoService videoService;

    @GetMapping("create")
    public VideoVo create(@RequestParam("videoId") String videoId) {
//        int val = count.getAndIncrement();
//        if (val % 3 == 0) {
//            count.set(1);
//            throw new RuntimeException();
//        }
//        return orderService.create(videoId);
        return videoService.getById(videoId);
    }

    AtomicInteger count = new AtomicInteger(1);

    @GetMapping("test")
    public String test() {
        int val = count.getAndIncrement();
        if (val % 10 == 0) {
            count.set(1);
            throw new RuntimeException();
        }
        return "OK";
    }
}
