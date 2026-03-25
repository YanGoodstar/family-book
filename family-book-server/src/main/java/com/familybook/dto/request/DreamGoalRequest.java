package com.familybook.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 梦想目标请求DTO
 */
@Data
public class DreamGoalRequest {

    /**
     * 目标ID（更新时使用）
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
     * 目标日期
     */
    private LocalDate targetDate;

    /**
     * 储蓄类型：1=固定金额, 2=工资百分比
     */
    private Integer savingsType;

    /**
     * 固定储蓄金额
     */
    private BigDecimal savingsAmount;

    /**
     * 储蓄百分比(如0.3表示30%)
     */
    private BigDecimal savingsPercent;

    /**
     * 月收入(百分比模式用)
     */
    private BigDecimal monthlyIncome;

    /**
     * 图标
     */
    private String icon;

    /**
     * 备注
     */
    private String remark;

    /**
     * 优先级
     */
    private Integer priority;
}
