package com.zr.video.dao.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (Video)实体类
 *
 * @author makejava
 * @since 2020-10-21 10:58:22
 */
@Data
public class VideoDo implements Serializable {
    private static final long serialVersionUID = 437186600564225347L;
    /**
     * 视频ID
     */
    private Integer id;
    /**
     * 视频标题
     */
    private String title;
    /**
     * 概述
     */
    private String summary;
    /**
     * 封面图
     */
    private String coverImg;
    /**
     * 价格,分
     */
    private Integer price;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 默认8.7，最高10分
     */
    private Double point;
}