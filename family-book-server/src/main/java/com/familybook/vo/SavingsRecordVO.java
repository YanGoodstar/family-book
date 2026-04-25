package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 储蓄记录视图对象
 */
@Data
public class SavingsRecordVO {

    /**
     * 记录ID
     */
    private String id;

    /**
     * 目标ID
     */
    private String goalId;

    /**
     * 记录月份(yyyy-MM)
     */
    private String recordMonth;

    /**
     * 计划储蓄金额
     */
    private BigDecimal plannedAmount;

    /**
     * 实际储蓄金额
     */
    private BigDecimal actualAmount;

    /**
     * 前端消费的金额字段
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否达标:0=否, 1=是
     */
    private Integer isCompleted;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
