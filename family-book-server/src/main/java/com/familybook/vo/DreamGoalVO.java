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
     * 截止日期（格式化字符串）
     */
    private String deadline;

    /**
     * 储蓄类型：1-固定金额，2-收入百分比
     */
    private Integer type;

    /**
     * 自动储蓄金额
     */
    private BigDecimal autoSaveAmount;

    /**
     * 自动储蓄百分比
     */
    private BigDecimal autoSavePercent;

    /**
     * 完成进度（百分比，如 50.00 表示50%）
     */
    private BigDecimal progress;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
