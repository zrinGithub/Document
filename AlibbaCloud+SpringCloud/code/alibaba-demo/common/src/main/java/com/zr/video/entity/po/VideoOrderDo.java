package com.zr.video.entity.po;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (VideoOrder)实体类
 *
 * @author makejava
 * @since 2020-10-21 10:44:04
 */
@Data
public class VideoOrderDo implements Serializable {
    private static final long serialVersionUID = 632890057961806915L;

    private Integer id;
    /**
     * 订单唯一标识
     */
    private String outTradeNo;
    /**
     * 0表示未支付，1表示已支付
     */
    private Integer state;
    /**
     * 订单生成时间
     */
    private Date createTime;
    /**
     * 支付金额，单位分
     */
    private Integer totalFee;
    /**
     * 视频主键
     */
    private Integer videoId;
    /**
     * 视频标题
     */
    private String videoTitle;
    /**
     * 视频图片
     */
    private String videoImg;
    /**
     * 用户id
     */
    private Integer userId;


}