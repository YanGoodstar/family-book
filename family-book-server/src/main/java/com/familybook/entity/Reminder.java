package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 记账提醒实体类
 * 对应数据库表 t_reminder
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_reminder")
public class Reminder extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 提醒类型：1=每日，2=每周，3=每月
     */
    private Integer type;

    /**
     * 提醒时间，格式如 "09:00"
     */
    private String remindTime;

    /**
     * 提醒内容
     */
    private String content;

    /**
     * 是否启用：0=禁用，1=启用
     */
    private Integer isEnabled;
}
