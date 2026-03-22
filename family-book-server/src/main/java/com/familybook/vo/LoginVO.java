package com.familybook.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private UserVO user;
}
