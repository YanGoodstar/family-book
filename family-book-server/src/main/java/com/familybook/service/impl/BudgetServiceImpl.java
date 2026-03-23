package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Budget;
import com.familybook.entity.Transaction;
import com.familybook.mapper.BudgetMapper;
import com.familybook.mapper.TransactionMapper;
import com.familybook.service.BudgetService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预算服务实现类
 */
@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements BudgetService {

    private final BudgetMapper budgetMapper;
    private final TransactionMapper transactionMapper;

    public BudgetServiceImpl(BudgetMapper budgetMapper, TransactionMapper transactionMapper) {
        this.budgetMapper = budgetMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Budget setBudget(Budget budget) {
        Long userId = budget.getUserId();
        String budgetMonth = budget.getBudgetMonth();
        Long categoryId = budget.getCategoryId();

        // 检查是否已存在同月份同分类的预算
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetMonth, budgetMonth)
                .eq(categoryId != null, Budget::getCategoryId, categoryId)
                .isNull(categoryId == null, Budget::getCategoryId);

        Budget existingBudget = this.getOne(wrapper);

        if (existingBudget != null) {
            // 更新现有预算
            existingBudget.setBudgetAmount(budget.getBudgetAmount());
            existingBudget.setAlertThreshold(budget.getAlertThreshold());
            this.updateById(existingBudget);
            return existingBudget;
        } else {
            // 新建预算
            budget.setIsAlerted(0);
            this.save(budget);
            return budget;
        }
    }

    @Override
    public List<Budget> getBudgets(Long userId, Long familyId, String budgetMonth) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetMonth, budgetMonth)
                .orderByAsc(Budget::getCategoryId);

        return this.list(wrapper);
    }

    @Override
    public BigDecimal getBudgetUsage(Long budgetId) {
        Budget budget = this.getById(budgetId);
        if (budget == null) {
            return BigDecimal.ZERO;
        }

        Long userId = budget.getUserId();
        String budgetMonth = budget.getBudgetMonth();
        Long categoryId = budget.getCategoryId();

        // 解析月份获取起始和结束日期
        YearMonth yearMonth = YearMonth.parse(budgetMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 查询该用户该月份该分类的支出总额
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
                .eq(Transaction::getType, 2) // 2=支出
                .between(Transaction::getTransactionDate, startDate, endDate);

        // 如果有分类限制，则按分类查询
        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }

        List<Transaction> transactions = transactionMapper.selectList(wrapper);

        // 汇总支出金额
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() != null) {
                totalExpense = totalExpense.add(transaction.getAmount());
            }
        }

        return totalExpense;
    }

    @Override
    public boolean checkBudgetAlert(Long budgetId) {
        Budget budget = this.getById(budgetId);
        if (budget == null) {
            return false;
        }

        BigDecimal budgetAmount = budget.getBudgetAmount();
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal usedAmount = getBudgetUsage(budgetId);
        BigDecimal usageRate = usedAmount.divide(budgetAmount, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal threshold = budget.getAlertThreshold() != null ? budget.getAlertThreshold() : new BigDecimal("0.8");

        boolean isAlert = usageRate.compareTo(threshold) >= 0;

        // 更新预警状态
        if (isAlert && budget.getIsAlerted() != null && budget.getIsAlerted() == 0) {
            budget.setIsAlerted(1);
            this.updateById(budget);
        }

        return isAlert;
    }
}
