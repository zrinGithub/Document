package com.zr.video.service;

import com.zr.video.entity.vo.VideoVo;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:06
 */
public interface VideoService {
    /**
     * 根据id查询视频信息
     * @param id
     * @return
     */
    VideoVo getById(String id);
}
