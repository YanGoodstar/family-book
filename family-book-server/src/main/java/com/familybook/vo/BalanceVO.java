package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceVO {

    private boolean loggedIn;

    private boolean initialBalanceSet;

    private BigDecimal initialBalance;

    private BigDecimal currentBalance;

    private BigDecimal committedSavings;

    private BigDecimal spendableBalance;

    private boolean overCommitted;
}
