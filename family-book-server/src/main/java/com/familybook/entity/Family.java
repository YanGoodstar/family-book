package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭组实体类
 * 对应数据库表 t_family
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_family")
public class Family extends BaseEntity {

    /**
     * 家庭组名称
     */
    private String name;

    /**
     * 家庭组邀请码（唯一，用于加入家庭）
     */
    private String code;

    /**
     * 家庭组头像URL
     */
    private String avatarUrl;

    /**
     * 创建者用户ID
     */
    private Long createdBy;
}
