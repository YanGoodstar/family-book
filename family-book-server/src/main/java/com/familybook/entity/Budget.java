package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 预算实体类
 * 对应数据库表 t_budget
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_budget")
public class Budget extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 家庭组ID
     */
    private Long familyId;

    /**
     * 分类ID，null表示总预算
     */
    private Long categoryId;

    /**
     * 预算年份
     */
    private Integer year;

    /**
     * 预算月份
     */
    private Integer month;

    /**
     * 预算金额
     */
    private BigDecimal amount;

    /**
     * 预警阈值（百分比，如80表示80%）
     */
    private BigDecimal alertThreshold;
}
