package net.xdclass.service.fallback;

import net.xdclass.domain.Video;
import net.xdclass.service.VideoService;
import org.springframework.stereotype.Service;

/**
 * @Description 旭瑶&小滴课堂 xdclass.net
 * @Author 二当家小D  代码、笔记和技术指导联系我即可
 * @Version 1.0
 **/

@Service
public class VideoServiceFallback implements VideoService {
    @Override
    public Video findById(int videoId) {

        Video video = new Video();
        video.setTitle("这个是Fallback里面的视频");

        return video;
    }

    @Override
    public int save(Video video) {
        return 0;
    }
}
