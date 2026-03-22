package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 交易记录实体类
 * 对应数据库表 t_transaction
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_transaction")
public class Transaction extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 家庭组ID（可为空，表示个人记录）
     */
    private Long familyId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 交易类型：1=收入，2=支出
     */
    private Integer type;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易描述/备注
     */
    private String description;

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
