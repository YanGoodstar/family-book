package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭成员实体类
 * 对应数据库表 t_family_member
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_family_member")
public class FamilyMember extends BaseEntity {

    /**
     * 家庭组ID
     */
    private Long familyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色：0-普通成员，1-管理员
     */
    private Integer role;

    /**
     * 是否为默认家庭：0-否，1-是
     */
    private Integer isDefault;
}
