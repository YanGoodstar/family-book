package com.familybook.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易记录视图对象
 * 返回给前端的记账记录详情
 */
@Data
public class TransactionVO {

    /**
     * 交易记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户名称
     */
    private String accountName;

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
     * 类型：1-收入，2-支出
     */
    private Integer type;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 描述/备注
     */
    private String description;

    /**
     * 交易日期（格式化字符串）
     */
    private String transactionDate;

    /**
     * 交易时间（格式化字符串）
     */
    private String transactionTime;

    /**
     * 交易地点
     */
    private String location;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
