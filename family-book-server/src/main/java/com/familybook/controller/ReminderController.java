package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.ReminderRequest;
import com.familybook.entity.Reminder;
import com.familybook.security.SecurityUtils;
import com.familybook.service.ReminderService;
import com.familybook.vo.ReminderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "记账提醒", description = "每日记账提醒设置")
@RestController
@RequestMapping("/api/v1/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @Operation(summary = "获取提醒列表", description = "获取当前用户的所有记账提醒")
    @GetMapping("/list")
    public Result<List<ReminderVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Reminder> reminders = reminderService.getUserReminders(userId);

        List<ReminderVO> voList = reminders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "创建提醒", description = "创建记账提醒，支持每天/工作日/周末")
    @PostMapping
    public Result<ReminderVO> create(@RequestBody ReminderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Reminder reminder = new Reminder();
        BeanUtils.copyProperties(request, reminder);
        reminder.setUserId(userId);

        // 解析时间字符串
        if (request.getRemindTime() != null) {
            reminder.setRemindTime(LocalTime.parse(request.getRemindTime(), DateTimeFormatter.ofPattern("HH:mm")));
        }

        // 设置默认启用
        if (reminder.getIsEnabled() == null) {
            reminder.setIsEnabled(1);
        }

        Reminder saved = reminderService.createReminder(reminder);

        ReminderVO vo = convertToVO(saved);
        return Result.success(vo);
    }

    @Operation(summary = "更新提醒", description = "更新提醒设置")
    @PutMapping("/{id}")
    public Result<ReminderVO> update(@PathVariable Long id, @RequestBody ReminderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Reminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.error("提醒不存在");
        }

        if (!reminder.getUserId().equals(userId)) {
            return Result.error("无权修改此提醒");
        }

        BeanUtils.copyProperties(request, reminder);
        reminder.setId(id);

        // 解析时间字符串
        if (request.getRemindTime() != null) {
            reminder.setRemindTime(LocalTime.parse(request.getRemindTime(), DateTimeFormatter.ofPattern("HH:mm")));
        }

        reminderService.updateById(reminder);

        ReminderVO vo = convertToVO(reminder);
        return Result.success(vo);
    }

    @Operation(summary = "删除提醒", description = "删除记账提醒")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Reminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.error("提醒不存在");
        }

        if (!reminder.getUserId().equals(userId)) {
            return Result.error("无权删除此提醒");
        }

        reminderService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用提醒", description = "切换提醒的启用状态")
    @PostMapping("/{id}/toggle")
    public Result<ReminderVO> toggle(@PathVariable Long id, @RequestParam boolean enabled) {
        Long userId = SecurityUtils.getCurrentUserId();

        Reminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.error("提醒不存在");
        }

        if (!reminder.getUserId().equals(userId)) {
            return Result.error("无权操作此提醒");
        }

        reminderService.toggleReminder(id, enabled);

        Reminder updated = reminderService.getById(id);
        ReminderVO vo = convertToVO(updated);
        return Result.success(vo);
    }

    @Operation(summary = "获取提醒详情", description = "根据ID获取提醒详情")
    @GetMapping("/{id}")
    public Result<ReminderVO> getById(@PathVariable Long id) {
        Reminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.error("提醒不存在");
        }

        ReminderVO vo = convertToVO(reminder);
        return Result.success(vo);
    }

    /**
     * 转换为VO
     */
    private ReminderVO convertToVO(Reminder reminder) {
        ReminderVO vo = new ReminderVO();
        BeanUtils.copyProperties(reminder, vo);

        // 格式化时间
        if (reminder.getRemindTime() != null) {
            vo.setRemindTime(reminder.getRemindTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        return vo;
    }
}
