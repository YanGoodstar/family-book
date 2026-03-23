package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.BudgetRequest;
import com.familybook.entity.Budget;
import com.familybook.security.SecurityUtils;
import com.familybook.service.BudgetService;
import com.familybook.vo.BudgetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "预算管理", description = "月度预算设置、预算使用统计、超支预警")
@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "设置预算", description = "设置月度总预算或分类预算")
    @PostMapping
    public Result<BudgetVO> setBudget(@RequestBody BudgetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Budget budget = new Budget();
        BeanUtils.copyProperties(request, budget);
        budget.setUserId(userId);

        // 设置默认月份
        if (budget.getBudgetMonth() == null) {
            budget.setBudgetMonth(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }

        // 设置默认预警阈值
        if (budget.getAlertThreshold() == null) {
            budget.setAlertThreshold(new BigDecimal("0.8")); // 默认80%预警
        }

        Budget saved = budgetService.setBudget(budget);

        BudgetVO vo = convertToVO(saved);
        return Result.success(vo);
    }

    @Operation(summary = "获取预算列表", description = "获取指定月份的预算列表，包括总预算和分类预算")
    @GetMapping("/list")
    public Result<List<BudgetVO>> list(
            @RequestParam(required = false) String budgetMonth,
            @RequestParam(required = false) Long familyId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 默认当前月份
        if (budgetMonth == null) {
            budgetMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<Budget> budgets = budgetService.getBudgets(userId, familyId, budgetMonth);

        List<BudgetVO> voList = budgets.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "获取预算详情", description = "根据ID获取预算详情及使用情况")
    @GetMapping("/{id}")
    public Result<BudgetVO> getById(@PathVariable Long id) {
        Budget budget = budgetService.getById(id);
        if (budget == null) {
            return Result.error("预算不存在");
        }

        BudgetVO vo = convertToVO(budget);
        return Result.success(vo);
    }

    @Operation(summary = "删除预算", description = "删除指定预算")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Budget budget = budgetService.getById(id);
        if (budget == null) {
            return Result.error("预算不存在");
        }

        if (!budget.getUserId().equals(userId)) {
            return Result.error("无权删除此预算");
        }

        budgetService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "检查预算预警", description = "检查指定预算是否触发预警")
    @GetMapping("/{id}/alert")
    public Result<Boolean> checkAlert(@PathVariable Long id) {
        boolean isAlert = budgetService.checkBudgetAlert(id);
        return Result.success(isAlert);
    }

    @Operation(summary = "获取月度预算概览", description = "获取指定月份的总预算使用情况")
    @GetMapping("/overview")
    public Result<BudgetVO> overview(
            @RequestParam(required = false) String budgetMonth,
            @RequestParam(required = false) Long familyId) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (budgetMonth == null) {
            budgetMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<Budget> budgets = budgetService.getBudgets(userId, familyId, budgetMonth);

        // 查找总预算（categoryId为null的预算）
        Budget totalBudget = budgets.stream()
                .filter(b -> b.getCategoryId() == null)
                .findFirst()
                .orElse(null);

        if (totalBudget == null) {
            return Result.error("该月份未设置总预算");
        }

        BudgetVO vo = convertToVO(totalBudget);
        return Result.success(vo);
    }

    /**
     * 转换为VO并计算使用情况
     */
    private BudgetVO convertToVO(Budget budget) {
        BudgetVO vo = new BudgetVO();
        BeanUtils.copyProperties(budget, vo);

        // 获取已使用金额
        BigDecimal usedAmount = budgetService.getBudgetUsage(budget.getId());
        vo.setUsedAmount(usedAmount);

        // 计算使用率
        if (budget.getBudgetAmount() != null && budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal usageRate = usedAmount
                    .multiply(new BigDecimal("100"))
                    .divide(budget.getBudgetAmount(), 2, RoundingMode.HALF_UP);
            vo.setUsageRate(usageRate);

            // 判断是否触发预警
            BigDecimal threshold = budget.getAlertThreshold() != null ? budget.getAlertThreshold() : new BigDecimal("0.8");
            BigDecimal thresholdPercent = threshold.multiply(new BigDecimal("100"));
            vo.setIsAlert(usageRate.compareTo(thresholdPercent) >= 0);
        } else {
            vo.setUsageRate(BigDecimal.ZERO);
            vo.setIsAlert(false);
        }

        return vo;
    }
}
