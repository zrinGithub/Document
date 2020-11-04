package com.zr.video.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 15:22
 */
@RestController
@RequestMapping("/config")
public class TestNacosConfigController {
    @Value("${customization.testVal}")
    private String testVal;

    @GetMapping("test")
    public String test() {
        return testVal;
    }
}
