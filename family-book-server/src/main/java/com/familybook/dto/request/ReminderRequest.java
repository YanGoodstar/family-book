package com.familybook.dto.request;

import lombok.Data;

/**
 * 记账提醒请求DTO
 */
@Data
public class ReminderRequest {

    /**
     * 提醒ID（更新时使用）
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
}
