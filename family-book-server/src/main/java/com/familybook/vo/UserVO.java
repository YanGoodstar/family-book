package com.familybook.vo;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 默认家庭组ID
     */
    private Long defaultFamilyId;

    /**
     * 创建时间
     */
    private String createTime;
}
