package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Reminder;
import com.familybook.mapper.ReminderMapper;
import com.familybook.service.ReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

/**
 * 记账提醒服务实现类
 */
@Service
public class ReminderServiceImpl extends ServiceImpl<ReminderMapper, Reminder> implements ReminderService {

    private final ReminderMapper reminderMapper;

    public ReminderServiceImpl(ReminderMapper reminderMapper) {
        this.reminderMapper = reminderMapper;
    }

    @Override
    public List<Reminder> getUserReminders(Long userId) {
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getUserId, userId)
                .orderByDesc(Reminder::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public Reminder createReminder(Reminder reminder) {
        // 参数校验
        if (reminder.getRemindTime() == null) {
            // 默认晚上8点提醒
            reminder.setRemindTime(LocalTime.of(20, 0));
        }

        if (reminder.getRemindType() == null) {
            reminder.setRemindType(1); // 默认每天提醒
        }

        if (reminder.getIsEnabled() == null) {
            reminder.setIsEnabled(1); // 默认启用
        }

        // 检查该用户是否已有相同时间相同类型的提醒
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getUserId, reminder.getUserId())
                .eq(Reminder::getRemindTime, reminder.getRemindTime())
                .eq(Reminder::getRemindType, reminder.getRemindType());

        Reminder existing = this.getOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("该时间段已存在相同类型的提醒");
        }

        this.save(reminder);
        return reminder;
    }

    @Override
    public void toggleReminder(Long id, boolean enabled) {
        Reminder reminder = this.getById(id);
        if (reminder == null) {
            throw new RuntimeException("提醒不存在");
        }

        reminder.setIsEnabled(enabled ? 1 : 0);
        this.updateById(reminder);
    }
}
