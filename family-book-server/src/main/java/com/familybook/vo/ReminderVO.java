package com.familybook.vo;

import lombok.Data;

/**
 * 记账提醒视图对象
 */
@Data
public class ReminderVO {

    /**
     * 提醒ID
     */
    private Long id;

    /**
     * 提醒时间（格式：HH:mm）
     */
    private String remindTime;

    /**
     * 提醒类型:1=每天, 2=工作日, 3=周末
     */
    private Integer remindType;

    /**
     * 是否启用：0=禁用，1=启用
     */
    private Integer isEnabled;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
