package com.familybook.dto.request;

import lombok.Data;

/**
 * 加入家庭组请求DTO
 */
@Data
public class JoinFamilyRequest {

    /**
     * 邀请码
     */
    private String code;
}
