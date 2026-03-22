package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 储蓄记录视图对象
 */
@Data
public class SavingsRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 梦想目标ID
     */
    private Long dreamGoalId;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份（1-12）
     */
    private Integer month;

    /**
     * 储蓄金额
     */
    private BigDecimal amount;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
