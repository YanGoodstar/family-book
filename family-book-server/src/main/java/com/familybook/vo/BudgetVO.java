package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算视图对象
 */
@Data
public class BudgetVO {

    /**
     * 预算ID
     */
    private Long id;

    /**
     * 分类ID（为空表示总预算）
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 预算年份
     */
    private Integer year;

    /**
     * 预算月份
     */
    private Integer month;

    /**
     * 预算金额
     */
    private BigDecimal amount;

    /**
     * 预警阈值
     */
    private BigDecimal alertThreshold;

    /**
     * 已使用金额
     */
    private BigDecimal usedAmount;

    /**
     * 使用率（百分比，如80.00表示80%）
     */
    private BigDecimal usageRate;

    /**
     * 是否触发预警
     */
    private Boolean isAlert;
}
