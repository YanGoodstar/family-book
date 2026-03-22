package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类统计视图对象
 * 用于展示按分类汇总的收支数据
 */
@Data
public class CategoryStatisticsVO {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类图标
     */
    private String categoryIcon;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 占比（百分比）
     */
    private BigDecimal percentage;
}
