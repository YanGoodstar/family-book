package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.DreamGoalRequest;
import com.familybook.entity.DreamGoal;
import com.familybook.security.SecurityUtils;
import com.familybook.service.DreamGoalService;
import com.familybook.vo.DreamGoalVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "梦想目标", description = "储蓄目标设定、进度跟踪、月度储蓄执行")
@RestController
@RequestMapping("/api/v1/dream-goal")
@RequiredArgsConstructor
public class DreamGoalController {

    private final DreamGoalService dreamGoalService;

    @Operation(summary = "创建梦想目标", description = "创建储蓄目标，支持固定金额或工资百分比模式")
    @PostMapping
    public Result<DreamGoalVO> create(@RequestBody DreamGoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = new DreamGoal();
        BeanUtils.copyProperties(request, dreamGoal);
        dreamGoal.setUserId(userId);
        dreamGoal.setSavedAmount(BigDecimal.ZERO); // 初始已存金额为0

        DreamGoal saved = dreamGoalService.createDreamGoal(dreamGoal);

        DreamGoalVO vo = convertToVO(saved);
        return Result.success(vo);
    }

    @Operation(summary = "获取梦想目标列表", description = "获取当前用户的所有梦想目标")
    @GetMapping("/list")
    public Result<List<DreamGoalVO>> list(@RequestParam(required = false) Long familyId) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<DreamGoal> goals = dreamGoalService.getUserDreamGoals(userId, familyId);

        List<DreamGoalVO> voList = goals.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "获取梦想目标详情", description = "根据ID获取梦想目标详情")
    @GetMapping("/{id}")
    public Result<DreamGoalVO> getById(@PathVariable Long id) {
        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        DreamGoalVO vo = convertToVO(dreamGoal);
        return Result.success(vo);
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

        BeanUtils.copyProperties(request, dreamGoal);
        dreamGoal.setId(id);
        dreamGoalService.updateById(dreamGoal);

        DreamGoalVO vo = convertToVO(dreamGoal);
        return Result.success(vo);
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

    @Operation(summary = "手动储蓄", description = "手动存入指定金额到梦想目标")
    @PostMapping("/{id}/save")
    public Result<DreamGoalVO> saveAmount(@PathVariable Long id, @RequestParam BigDecimal amount) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权操作此目标");
        }

        dreamGoalService.executeMonthlySaving(id, amount);

        // 重新获取最新数据
        DreamGoal updated = dreamGoalService.getById(id);
        DreamGoalVO vo = convertToVO(updated);
        return Result.success(vo);
    }

    @Operation(summary = "获取目标看板", description = "获取梦想目标的完整看板数据")
    @GetMapping("/{id}/dashboard")
    public Result<Map<String, Object>> dashboard(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        DreamGoal dreamGoal = dreamGoalService.getById(id);
        if (dreamGoal == null) {
            return Result.error("梦想目标不存在");
        }

        if (!dreamGoal.getUserId().equals(userId)) {
            return Result.error("无权查看此目标");
        }

        Map<String, Object> dashboard = new HashMap<>();

        // 基本信息
        dashboard.put("goal", convertToVO(dreamGoal));

        // 计算预计完成时间
        BigDecimal targetAmount = dreamGoal.getTargetAmount();
        BigDecimal savedAmount = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = targetAmount.subtract(savedAmount);

        // 每月应存金额
        BigDecimal monthlySaving = calculateMonthlySaving(dreamGoal);
        dashboard.put("monthlySaving", monthlySaving);

        // 预计剩余月数
        if (monthlySaving.compareTo(BigDecimal.ZERO) > 0) {
            int remainingMonths = remainingAmount.divide(monthlySaving, 0, RoundingMode.CEILING).intValue();
            dashboard.put("remainingMonths", remainingMonths);
        } else {
            dashboard.put("remainingMonths", 0);
        }

        // 完成状态
        boolean isCompleted = savedAmount.compareTo(targetAmount) >= 0;
        dashboard.put("isCompleted", isCompleted);

        return Result.success(dashboard);
    }

    /**
     * 转换为VO并计算进度
     */
    private DreamGoalVO convertToVO(DreamGoal dreamGoal) {
        DreamGoalVO vo = new DreamGoalVO();
        BeanUtils.copyProperties(dreamGoal, vo);

        // 计算完成进度
        BigDecimal targetAmount = dreamGoal.getTargetAmount();
        BigDecimal savedAmount = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;

        if (targetAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = savedAmount
                    .multiply(new BigDecimal("100"))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP);
            vo.setProgress(progress);
        } else {
            vo.setProgress(BigDecimal.ZERO);
        }

        // 格式化日期
        if (dreamGoal.getTargetDate() != null) {
            vo.setTargetDate(dreamGoal.getTargetDate().toString());
        }

        return vo;
    }

    /**
     * 计算每月应存金额
     */
    private BigDecimal calculateMonthlySaving(DreamGoal dreamGoal) {
        if (dreamGoal.getSavingsType() == null) {
            return BigDecimal.ZERO;
        }

        if (dreamGoal.getSavingsType() == 1) {
            // 固定金额模式
            return dreamGoal.getSavingsAmount() != null ? dreamGoal.getSavingsAmount() : BigDecimal.ZERO;
        } else if (dreamGoal.getSavingsType() == 2) {
            // 工资百分比模式
            BigDecimal monthlyIncome = dreamGoal.getMonthlyIncome() != null ? dreamGoal.getMonthlyIncome() : BigDecimal.ZERO;
            BigDecimal savingsPercent = dreamGoal.getSavingsPercent() != null ? dreamGoal.getSavingsPercent() : BigDecimal.ZERO;
            return monthlyIncome.multiply(savingsPercent);
        }

        return BigDecimal.ZERO;
    }
}
