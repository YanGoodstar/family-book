package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 转账记录实体类
 * 记录用户账户之间的转账操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_transfer")
public class Transfer extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

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
