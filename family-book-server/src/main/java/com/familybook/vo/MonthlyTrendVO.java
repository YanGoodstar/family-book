package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度趋势视图对象
 * 用于展示月度收支趋势图表数据
 */
@Data
public class MonthlyTrendVO {

    /**
     * 月份(yyyy-MM)
     */
    private String month;

    /**
     * 收入
     */
    private BigDecimal income;

    /**
     * 支出
     */
    private BigDecimal expense;

    /**
     * 结余
     */
    private BigDecimal balance;
}
