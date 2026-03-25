package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 梦想目标详情聚合视图
 */
@Data
public class DreamGoalDashboardVO {

    /**
     * 目标信息
     */
    private DreamGoalVO goal;

    /**
     * 剩余金额
     */
    private BigDecimal remainingAmount;

    /**
     * 是否完成
     */
    private Boolean completed;

    /**
     * 最近存钱记录
     */
    private List<SavingsRecordVO> records;
}
