package com.familybook.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 设置起始金额请求
 */
@Data
public class BalanceRequest {

    /**
     * 起始金额
     */
    private BigDecimal initialBalance;
}
