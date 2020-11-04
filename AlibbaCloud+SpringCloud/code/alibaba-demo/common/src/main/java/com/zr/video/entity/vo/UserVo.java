package com.zr.video.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2020-10-21 10:56:03
 */
@Data
@ApiModel(description = "信息类")
public class UserVo implements Serializable {
    private static final long serialVersionUID = 534811536080890254L;
    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private Object id;
    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String phone;
    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String pwd;
    /**
     * 性别 男0 女1
     */
    @ApiModelProperty("性别 男0 女1")
    private Integer sex;
    /**
     *
     */
    @ApiModelProperty("")
    private String img;
    /**
     *
     */
    @ApiModelProperty("")
    private Date createTime;
    /**
     * 1是普通用户，2是管理员
     */
    @ApiModelProperty("1是普通用户，2是管理员")
    private Integer role;
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("$column.comment")
    private String wechat;


}