package com.zr.video.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (Video)实体类
 *
 * @author makejava
 * @since 2020-10-21 10:58:23
 */
@Data
@ApiModel(description = "信息类")
public class VideoVo implements Serializable {
    private static final long serialVersionUID = 964908317624301803L;
    /**
     * 视频ID
     */
    @ApiModelProperty("视频ID")
    private Integer id;
    /**
     * 视频标题
     */
    @ApiModelProperty("视频标题")
    private String title;
    /**
     * 概述
     */
    @ApiModelProperty("概述")
    private String summary;
    /**
     * 封面图
     */
    @ApiModelProperty("封面图")
    private String coverImg;
    /**
     * 价格,分
     */
    @ApiModelProperty("价格,分")
    private Integer price;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
     * 默认8.7，最高10分
     */
    @ApiModelProperty("默认8.7，最高10分")
    private Double point;

    @ApiModelProperty("服务地址")
    private String serverAddr;
}