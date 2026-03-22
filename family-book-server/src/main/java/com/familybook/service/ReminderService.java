package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Reminder;

import java.util.List;

/**
 * 记账提醒服务接口
 */
public interface ReminderService extends IService<Reminder> {

    /**
     * 获取用户提醒列表
     */
    List<Reminder> getUserReminders(Long userId);

    /**
     * 创建提醒
     */
    Reminder createReminder(Reminder reminder);

    /**
     * 启用/禁用提醒
     */
    void toggleReminder(Long id, boolean enabled);
}
