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
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 备注
     */
    private String remark;

    /**
     * 转账日期（格式化字符串）
     */
    private String transferDate;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
