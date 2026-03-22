package com.familybook.vo;

import lombok.Data;

/**
 * 家庭组视图对象
 */
@Data
public class FamilyVO {

    /**
     * 家庭组ID
     */
    private Long id;

    /**
     * 家庭组名称
     */
    private String name;

    /**
     * 邀请码
     */
    private String code;

    /**
     * 家庭组头像URL
     */
    private String avatarUrl;

    /**
     * 创建人用户ID
     */
    private Long createdBy;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
