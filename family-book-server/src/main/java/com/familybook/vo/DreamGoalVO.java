package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 梦想目标视图对象
 */
@Data
public class DreamGoalVO {

    /**
     * 目标ID
     */
    private Long id;

    /**
     * 目标名称
     */
    private String name;

    /**
     * 目标金额
     */
    private BigDecimal targetAmount;

    /**
     * 已储蓄金额
     */
    private BigDecimal savedAmount;

    /**
     * 目标日期（格式化字符串）
     */
    private String targetDate;

    /**
     * 储蓄类型：1=固定金额, 2=工资百分比
     */
    private Integer savingsType;

    /**
     * 固定储蓄金额
     */
    private BigDecimal savingsAmount;

    /**
     * 储蓄百分比
     */
    private BigDecimal savingsPercent;

    /**
     * 月收入
     */
    private BigDecimal monthlyIncome;

    /**
     * 图标
     */
    private String icon;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 完成进度（百分比）
     */
    private BigDecimal progress;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
