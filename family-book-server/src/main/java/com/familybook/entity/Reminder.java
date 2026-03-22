package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

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
     * 提醒时间
     */
    private LocalTime remindTime;

    /**
     * 提醒类型:1=每天, 2=工作日, 3=周末
     */
    private Integer remindType;

    /**
     * 是否启用:0=否, 1=是
     */
    private Integer isEnabled;
}
