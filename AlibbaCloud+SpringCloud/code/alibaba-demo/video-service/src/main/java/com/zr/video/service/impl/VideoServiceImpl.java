package com.zr.video.service.impl;

import com.zr.video.dao.VideoMapper;
import com.zr.video.entity.vo.VideoVo;
import com.zr.video.service.VideoService;
import com.zr.video.util.SpringBeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Description:
 *
 * @author zhangr
 * 2020/10/21 11:11
 */
@Service
public class VideoServiceImpl implements VideoService {
    @Resource
    private VideoMapper videoMapper;

    @Override
    public VideoVo getById(String id) {
        return SpringBeanUtils.copyProperties(videoMapper.getById(id), VideoVo.class);
    }
}
