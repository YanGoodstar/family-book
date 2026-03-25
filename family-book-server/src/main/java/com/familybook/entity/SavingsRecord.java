package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 储蓄记录实体类
 * 记录每月自动储蓄的金额，关联到梦想目标
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_savings_record")
public class SavingsRecord extends BaseEntity {

    /**
     * 目标ID
     */
    private Long goalId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 记录月份(yyyy-MM)
     */
    private String recordMonth;

    /**
     * 计划储蓄金额
     */
    private BigDecimal plannedAmount;

    /**
     * 实际储蓄金额
     */
    private BigDecimal actualAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否达标:0=否, 1=是
     */
    private Integer isCompleted;
}
