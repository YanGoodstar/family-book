package com.familybook.dto.request;

import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
public class LoginRequest {

    /**
     * 微信登录code
     */
    private String code;
}
