package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 余额视图对象
 */
@Data
public class BalanceVO {

    /**
     * 起始金额
     */
    private BigDecimal initialBalance;

    /**
     * 当前余额
     */
    private BigDecimal currentBalance;

    /**
     * 承诺储蓄
     */
    private BigDecimal committedSavings;

    /**
     * 可支配余额
     */
    private BigDecimal spendableBalance;

    /**
     * 是否超出承诺
     */
    private boolean overCommitted;
}
