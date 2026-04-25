package com.familybook.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 梦想目标存钱请求
 */
@Data
public class DreamGoalSaveRequest {

    /**
     * 存入金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;
}
