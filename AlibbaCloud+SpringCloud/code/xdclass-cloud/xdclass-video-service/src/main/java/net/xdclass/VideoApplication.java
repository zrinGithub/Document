package net.xdclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Description 旭瑶&小滴课堂 xdclass.net
 * @Author 二当家小D  代码、笔记和技术指导联系我即可
 * @Version 1.0
 **/

@SpringBootApplication
@MapperScan("net.xdclass.dao")
@EnableDiscoveryClient
public class VideoApplication {

    public static void main(String [] args){

        SpringApplication.run(VideoApplication.class,args);
    }


}
