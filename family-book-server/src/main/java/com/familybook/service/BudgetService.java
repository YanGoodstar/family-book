package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Budget;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算服务接口
 */
public interface BudgetService extends IService<Budget> {

    /**
     * 设置预算
     */
    Budget setBudget(Budget budget);

    /**
     * 获取预算列表
     */
    List<Budget> getBudgets(Long userId, Long familyId, String budgetMonth);

    /**
     * 获取预算使用情况
     */
    BigDecimal getBudgetUsage(Long budgetId);

    /**
     * 检查预算预警
     */
    boolean checkBudgetAlert(Long budgetId);
}
