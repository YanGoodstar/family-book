package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账户实体类
 * 对应数据库表 t_account
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_account")
public class Account extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 家庭组ID，为空表示个人账户
     */
    private Long familyId;

    /**
     * 账户名称
     */
    private String name;

    /**
     * 账户类型：1=现金, 2=储蓄卡, 3=信用卡, 4=支付宝, 5=微信, 6=其他
     */
    private Integer type;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 账户图标
     */
    private String icon;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
