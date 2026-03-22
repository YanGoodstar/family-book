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
     * 已储蓄金额
     */
    private BigDecimal savedAmount;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 储蓄类型：1=固定金额，2=收入百分比
     */
    private Integer type;

    /**
     * 自动储蓄固定金额（type=1时使用）
     */
    private BigDecimal autoSaveAmount;

    /**
     * 自动储蓄百分比（type=2时使用）
     */
    private Integer autoSavePercent;
}
