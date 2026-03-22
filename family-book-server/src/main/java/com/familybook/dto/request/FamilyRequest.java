package com.familybook.dto.request;

import lombok.Data;

/**
 * 家庭组请求DTO
 */
@Data
public class FamilyRequest {

    /**
     * 家庭组ID（更新时使用）
     */
    private Long id;

    /**
     * 家庭组名称
     */
    private String name;

    /**
     * 家庭组头像URL
     */
    private String avatarUrl;
}
