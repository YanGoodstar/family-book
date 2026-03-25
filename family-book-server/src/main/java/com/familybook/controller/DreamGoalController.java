package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.DreamGoalRequest;
import com.familybook.dto.request.DreamGoalSaveRequest;
import com.familybook.entity.DreamGoal;
import com.familybook.entity.SavingsRecord;
import com.familybook.security.SecurityUtils;
import com.familybook.service.DreamGoalService;
import com.familybook.service.SavingsRecordService;
import com.familybook.vo.DreamGoalDashboardVO;
import com.familybook.vo.DreamGoalVO;
import com.familybook.vo.SavingsRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "梦想目标", description = "个人梦想目标与手动存钱记录")
@RestController
@RequestMapping("/api/v1/dream-goal")
@RequiredArgsConstructor
public class DreamGoalController {

    private final DreamGoalService dreamGoalService;
    private final SavingsRecordService savingsRecordService;

    @Operation(summary = "创建梦想目标", description = "创建个人梦想目标")
    @PostMapping
    public Result<DreamGoalVO> create(@RequestBody DreamGoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = new DreamGoal();
        applyCreateFields(request, dreamGoal);
        dreamGoal.setUserId(userId);
        dreamGoal.setSavedAmount(BigDecimal.ZERO);

        DreamGoal saved = dreamGoalService.createDreamGoal(dreamGoal);
        return Result.success(convertToVO(saved));
    }

    @Operation(summary = "获取梦想目标列表", description = "获取当前用户的所有梦想目标")
    @GetMapping("/list")
    public Result<List<DreamGoalVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<DreamGoal> goals = dreamGoalService.getUserDreamGoals(userId);

        return Result.success(goals.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "获取梦想目标详情", description = "根据ID获取梦想目标详情")
    @GetMapping("/{id}")
    public Result<DreamGoalVO> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }
        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权查看此目标");
        }

        return Result.success(convertToVO(dreamGoal));
    }

    @Operation(summary = "更新梦想目标", description = "更新梦想目标信息")
    @PutMapping("/{id}")
    public Result<DreamGoalVO> update(@PathVariable Long id, @RequestBody DreamGoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权修改此目标");
        }

        applyUpdateFields(request, dreamGoal);
        dreamGoalService.updateById(dreamGoal);

        return Result.success(convertToVO(dreamGoal));
    }

    @Operation(summary = "删除梦想目标", description = "删除梦想目标")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权删除此目标");
        }

        dreamGoalService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "记录一笔存钱", description = "给梦想目标新增一笔手动存钱记录")
    @PostMapping("/{id}/save")
    public Result<DreamGoalDashboardVO> saveAmount(@PathVariable Long id, @RequestBody DreamGoalSaveRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权操作此目标");
        }

        DreamGoal updated = dreamGoalService.saveAmount(id, request.getAmount(), normalizeText(request.getRemark()));
        return Result.success(buildDashboard(updated));
    }

    @Operation(summary = "获取目标详情聚合数据", description = "获取目标详情、进度与最近存钱记录")
    @GetMapping("/{id}/dashboard")
    public Result<DreamGoalDashboardVO> dashboard(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权查看此目标");
        }

        return Result.success(buildDashboard(dreamGoal));
    }

    /**
     * 转换为VO并计算进度
     */
    private DreamGoalVO convertToVO(DreamGoal dreamGoal) {
        DreamGoalVO vo = new DreamGoalVO();
        BeanUtils.copyProperties(dreamGoal, vo);
        if (dreamGoal.getId() != null) {
            vo.setId(String.valueOf(dreamGoal.getId()));
        }

        // 计算完成进度
        BigDecimal targetAmount = dreamGoal.getTargetAmount();
        BigDecimal savedAmount = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;

        if (targetAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = savedAmount
                    .multiply(new BigDecimal("100"))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP);
            vo.setProgress(progress.min(new BigDecimal("100.00")));
        } else {
            vo.setProgress(BigDecimal.ZERO);
        }

        if (dreamGoal.getTargetDate() != null) {
            vo.setTargetDate(dreamGoal.getTargetDate().toString());
        }
        if (dreamGoal.getCreateTime() != null) {
            vo.setCreateTime(dreamGoal.getCreateTime().toString().replace('T', ' '));
        }

        BigDecimal remainingAmount = BigDecimal.ZERO;
        if (targetAmount != null) {
            remainingAmount = targetAmount.subtract(savedAmount);
            if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
                remainingAmount = BigDecimal.ZERO;
            }
        }
        boolean completed = isCompleted(savedAmount, targetAmount);
        vo.setRemainingAmount(remainingAmount);
        vo.setCompleted(completed);
        vo.setStatus(completed ? 2 : 1);

        return vo;
    }

    private DreamGoalDashboardVO buildDashboard(DreamGoal dreamGoal) {
        DreamGoalVO goalVO = convertToVO(dreamGoal);

        DreamGoalDashboardVO dashboardVO = new DreamGoalDashboardVO();
        dashboardVO.setGoal(goalVO);
        dashboardVO.setRemainingAmount(goalVO.getRemainingAmount());
        dashboardVO.setCompleted(Boolean.TRUE.equals(goalVO.getCompleted()));
        dashboardVO.setRecords(savingsRecordService.getRecentRecordsByGoalId(dreamGoal.getId(), 20).stream()
                .map(this::convertRecordToVO)
                .collect(Collectors.toList()));

        return dashboardVO;
    }

    private SavingsRecordVO convertRecordToVO(SavingsRecord record) {
        SavingsRecordVO vo = new SavingsRecordVO();
        BeanUtils.copyProperties(record, vo);
        if (record.getId() != null) {
            vo.setId(String.valueOf(record.getId()));
        }
        if (record.getGoalId() != null) {
            vo.setGoalId(String.valueOf(record.getGoalId()));
        }
        vo.setAmount(record.getActualAmount());
        if (record.getCreateTime() != null) {
            vo.setCreateTime(record.getCreateTime().toString().replace('T', ' '));
        }
        return vo;
    }

    private void applyCreateFields(DreamGoalRequest request, DreamGoal dreamGoal) {
        applyUpdateFields(request, dreamGoal);
        if (request.getSavingsType() != null) {
            dreamGoal.setSavingsType(request.getSavingsType());
        }
        if (request.getSavingsAmount() != null) {
            dreamGoal.setSavingsAmount(request.getSavingsAmount());
        }
        if (request.getSavingsPercent() != null) {
            dreamGoal.setSavingsPercent(request.getSavingsPercent());
        }
        if (request.getMonthlyIncome() != null) {
            dreamGoal.setMonthlyIncome(request.getMonthlyIncome());
        }
        if (request.getPriority() != null) {
            dreamGoal.setPriority(request.getPriority());
        }
    }

    private void applyUpdateFields(DreamGoalRequest request, DreamGoal dreamGoal) {
        String icon = normalizeText(request.getIcon());
        dreamGoal.setName(request.getName());
        dreamGoal.setTargetAmount(request.getTargetAmount());
        dreamGoal.setTargetDate(request.getTargetDate());
        dreamGoal.setIcon(icon != null ? icon : "🎯");
        dreamGoal.setRemark(normalizeText(request.getRemark()));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isCompleted(BigDecimal savedAmount, BigDecimal targetAmount) {
        BigDecimal safeSavedAmount = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        BigDecimal safeTargetAmount = targetAmount != null ? targetAmount : BigDecimal.ZERO;
        return safeTargetAmount.compareTo(BigDecimal.ZERO) > 0 && safeSavedAmount.compareTo(safeTargetAmount) >= 0;
    }
}
