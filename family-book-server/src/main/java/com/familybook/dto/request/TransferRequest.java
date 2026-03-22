package com.familybook.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 转账请求DTO
 */
@Data
public class TransferRequest {

    /**
     * 转账记录ID（新增时为空，更新时必填）
     */
    private Long id;

    /**
     * 转出账户ID
     */
    private Long fromAccountId;

    /**
     * 转入账户ID
     */
    private Long toAccountId;

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
     * 转账日期
     */
    private LocalDate transferDate;
}
