package com.zr.order.service;

import com.zr.video.entity.vo.VideoVo;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:34
 */
public interface OrderService {
    VideoVo create(String videoId);
}
