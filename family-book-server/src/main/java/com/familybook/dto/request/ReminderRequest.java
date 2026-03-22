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
     * 提醒类型：1-每日，2-每周，3-每月
     */
    private Integer type;

    /**
     * 提醒时间（格式：HH:mm）
     */
    private String remindTime;

    /**
     * 提醒内容
     */
    private String content;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isEnabled;
}
