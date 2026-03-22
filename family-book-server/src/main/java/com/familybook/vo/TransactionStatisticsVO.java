package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易统计视图对象
 * 用于展示收支汇总数据
 */
@Data
public class TransactionStatisticsVO {

    /**
     * 总收入
     */
    private BigDecimal income;

    /**
     * 总支出
     */
    private BigDecimal expense;

    /**
     * 结余（收入 - 支出）
     */
    private BigDecimal balance;
}
