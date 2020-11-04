package com.zr.order.service.impl;

import com.zr.order.service.OrderService;
import com.zr.video.entity.vo.VideoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:36
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private DiscoveryClient discoveryClient;

    @Override
    public VideoVo create(String videoId) {
        VideoVo videoVo = restTemplate.getForObject("http://video-service/video/getById?id=" + videoId, VideoVo.class);

        List<ServiceInstance> instances = discoveryClient.getInstances("video-service");
        List<String> services = discoveryClient.getServices();
        return videoVo;
    }
}
