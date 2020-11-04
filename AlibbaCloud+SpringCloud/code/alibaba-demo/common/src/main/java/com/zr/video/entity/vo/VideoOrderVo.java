package com.zr.video.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (VideoOrder)实体类
 *
 * @author makejava
 * @since 2020-10-21 10:44:11
 */
@Data
@ApiModel(description = "订单类")
public class VideoOrderVo implements Serializable {
    private static final long serialVersionUID = 570352464038132274L;

    @ApiModelProperty("$column.comment")
    private Integer id;

    @ApiModelProperty("订单唯一标识")
    private String outTradeNo;

    @ApiModelProperty("0表示未支付，1表示已支付")
    private Integer state;

    @ApiModelProperty("订单生成时间")
    private Date createTime;

    @ApiModelProperty("支付金额，单位分")
    private Integer totalFee;

    @ApiModelProperty("视频主键")
    private Integer videoId;

    @ApiModelProperty("视频标题")
    private String videoTitle;

    @ApiModelProperty("视频图片")
    private String videoImg;

    @ApiModelProperty("用户id")
    private Integer userId;
}