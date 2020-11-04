package com.zr.order.service.fallback;

import com.zr.order.service.VideoService;
import com.zr.video.entity.vo.VideoVo;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 服务异常默认处理类，这里还需要在配置文件里面开启sentinel对feign的支持
 *
 * @author zhangr
 * 2020/10/26 11:35
 */
@Service
public class VideoServiceFallBack implements VideoService {
    @Override
    public VideoVo getById(String id) {
        VideoVo videoVo = new VideoVo();
        videoVo.setTitle("未提供服务");
        return videoVo;
    }
}
