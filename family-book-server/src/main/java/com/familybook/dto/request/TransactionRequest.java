package com.familybook.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 交易记录请求DTO
 * 用于创建和更新记账记录
 */
@Data
public class TransactionRequest {

    /**
     * 交易记录ID（更新时使用）
     */
    private Long id;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 交易类型：1=支出，2=收入
     */
    private Integer type;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 交易日期
     */
    private LocalDate transactionDate;

    /**
     * 交易时间
     */
    private LocalTime transactionTime;

    /**
     * 交易地点
     */
    private String location;
}
