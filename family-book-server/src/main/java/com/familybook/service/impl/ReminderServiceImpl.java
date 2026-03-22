package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Reminder;
import com.familybook.mapper.ReminderMapper;
import com.familybook.service.ReminderService;
import org.springframework.stereotype.Service;

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
        return null;
    }

    @Override
    public Reminder createReminder(Reminder reminder) {
        return null;
    }

    @Override
    public void toggleReminder(Long id, boolean enabled) {

    }
}
