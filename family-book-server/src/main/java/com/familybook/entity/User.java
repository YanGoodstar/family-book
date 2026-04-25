package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 对应数据库表 t_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    /**
     * 微信openid，唯一标识
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
     * 起始金额
     */
    private java.math.BigDecimal initialBalance;

    /**
     * 是否已设置起始金额
     */
    private Boolean initialBalanceSet;

    /**
     * 当前余额
     */
    private java.math.BigDecimal currentBalance;
}
