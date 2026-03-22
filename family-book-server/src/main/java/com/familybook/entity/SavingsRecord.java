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
     * 梦想目标ID
     */
    private Long dreamGoalId;

    /**
     * 储蓄年份
     */
    private Integer year;

    /**
     * 储蓄月份（1-12）
     */
    private Integer month;

    /**
     * 储蓄金额
     */
    private BigDecimal amount;
}
