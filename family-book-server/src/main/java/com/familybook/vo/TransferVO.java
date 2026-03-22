package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账记录视图对象
 */
@Data
public class TransferVO {

    /**
     * 转账记录ID
     */
    private Long id;

    /**
     * 转出账户ID
     */
    private Long fromAccountId;

    /**
     * 转出账户名称
     */
    private String fromAccountName;

    /**
     * 转入账户ID
     */
    private Long toAccountId;

    /**
     * 转入账户名称
     */
    private String toAccountName;

    /**
     * 转账金额
     */
    private BigDecimal amount;

    /**
     * 转账说明
     */
    private String description;

    /**
     * 转账日期（格式化字符串）
     */
    private String transferDate;

    /**
     * 转账时间（格式化字符串）
     */
    private String transferTime;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
