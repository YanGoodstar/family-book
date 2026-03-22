package com.familybook.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算设置请求DTO
 */
@Data
public class BudgetRequest {

    /**
     * 预算ID（新增时为空，更新时必填）
     */
    private Long id;

    /**
     * 分类ID（为空表示总预算）
     */
    private Long categoryId;

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
     * 预警阈值（如0.8表示使用80%时预警）
     */
    private BigDecimal alertThreshold;
}
