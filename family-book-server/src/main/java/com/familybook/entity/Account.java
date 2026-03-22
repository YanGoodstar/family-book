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
     * 信用额度(信用卡)
     */
    private BigDecimal creditLimit;

    /**
     * 账单日(信用卡)
     */
    private Integer billDay;

    /**
     * 还款日(信用卡)
     */
    private Integer repayDay;

    /**
     * 账户图标
     */
    private String icon;

    /**
     * 是否默认账户:0=否, 1=是
     */
    private Integer isDefault;

    /**
     * 排序
     */
    private Integer sort;
}
