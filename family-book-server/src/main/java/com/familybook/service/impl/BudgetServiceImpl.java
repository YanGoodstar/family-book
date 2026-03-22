package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Budget;
import com.familybook.mapper.BudgetMapper;
import com.familybook.service.BudgetService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算服务实现类
 */
@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements BudgetService {

    private final BudgetMapper budgetMapper;

    public BudgetServiceImpl(BudgetMapper budgetMapper) {
        this.budgetMapper = budgetMapper;
    }

    @Override
    public Budget setBudget(Budget budget) {
        return null;
    }

    @Override
    public List<Budget> getBudgets(Long userId, Long familyId, String budgetMonth) {
        return null;
    }

    @Override
    public BigDecimal getBudgetUsage(Long budgetId) {
        return null;
    }

    @Override
    public boolean checkBudgetAlert(Long budgetId) {
        return false;
    }
}
