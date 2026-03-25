package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 梦想目标实体类
 * 对应数据库表 t_dream_goal
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_dream_goal")
public class DreamGoal extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 家庭组ID（可为空，表示个人目标）
     */
    private Long familyId;

    /**
     * 目标名称
     */
    private String name;

    /**
     * 目标金额
     */
    private BigDecimal targetAmount;

    /**
     * 已存金额
     */
    private BigDecimal savedAmount;

    /**
     * 目标日期
     */
    private LocalDate targetDate;

    /**
     * 储蓄类型：1=固定金额, 2=工资百分比
     */
    private Integer savingsType;

    /**
     * 固定储蓄金额
     */
    private BigDecimal savingsAmount;

    /**
     * 储蓄百分比(如0.3表示30%)
     */
    private BigDecimal savingsPercent;

    /**
     * 月收入(百分比模式用)
     */
    private BigDecimal monthlyIncome;

    /**
     * 图标
     */
    private String icon;

    /**
     * 备注
     */
    private String remark;

    /**
     * 优先级
     */
    private Integer priority;
}
