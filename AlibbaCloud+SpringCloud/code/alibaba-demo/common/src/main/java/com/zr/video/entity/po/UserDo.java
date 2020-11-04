package com.zr.video.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2020-10-21 10:56:00
 */
@Data
public class UserDo implements Serializable {
    private static final long serialVersionUID = 883309976156167512L;
    /**
     * 用户ID
     */
    private Object id;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 密码
     */
    private String pwd;
    /**
     * 性别 男0 女1
     */
    private Integer sex;
    /**
     *
     */
    private String img;
    /**
     *
     */
    private Date createTime;
    /**
     * 1是普通用户，2是管理员
     */
    private Integer role;
    /**
     * 用户名
     */
    private String username;

    private String wechat;
}