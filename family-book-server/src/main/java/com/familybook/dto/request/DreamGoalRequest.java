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
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 储蓄类型：1-固定金额，2-收入百分比
     */
    private Integer type;

    /**
     * 自动储蓄金额（type=1时使用）
     */
    private BigDecimal autoSaveAmount;

    /**
     * 自动储蓄百分比（type=2时使用）
     */
    private BigDecimal autoSavePercent;
}
