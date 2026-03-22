package com.familybook.vo;

import lombok.Data;

/**
 * 家庭成员视图对象
 */
@Data
public class FamilyMemberVO {

    /**
     * 成员记录ID
     */
    private Long id;

    /**
     * 家庭组ID
     */
    private Long familyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 角色：0-普通成员，1-管理员
     */
    private Integer role;

    /**
     * 是否默认家庭：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 加入时间（格式化字符串）
     */
    private String createTime;
}
