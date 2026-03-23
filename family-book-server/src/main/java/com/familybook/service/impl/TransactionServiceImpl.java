package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Account;
import com.familybook.entity.Category;
import com.familybook.entity.Transaction;
import com.familybook.mapper.AccountMapper;
import com.familybook.mapper.CategoryMapper;
import com.familybook.mapper.TransactionMapper;
import com.familybook.service.TransactionService;
import com.familybook.vo.CategoryStatisticsVO;
import com.familybook.vo.MonthlyTrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction record(Transaction transaction) {
        // 1. 保存交易记录
        transactionMapper.insert(transaction);

        // 2. 原子操作更新账户余额
        Account account = accountMapper.selectById(transaction.getAccountId());
        if (account != null) {
            BigDecimal newBalance;
            // type: 1=支出, 2=收入
            if (transaction.getType() == 1) {
                newBalance = account.getBalance().subtract(transaction.getAmount());
            } else {
                newBalance = account.getBalance().add(transaction.getAmount());
            }
            account.setBalance(newBalance);
            accountMapper.updateById(account);
        }

        return transaction;
    }

    @Override
    public List<Transaction> getTransactions(Long userId, Long familyId, LocalDate startDate, LocalDate endDate, Integer type, Long categoryId) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();

        // 个人数据或家庭共享数据
        wrapper.and(w -> w.eq(Transaction::getUserId, userId)
                         .or().eq(Transaction::getFamilyId, familyId));

        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }
        if (type != null) {
            wrapper.eq(Transaction::getType, type);
        }
        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Transaction::getTransactionDate)
               .orderByDesc(Transaction::getCreateTime);

        return transactionMapper.selectList(wrapper);
    }

    @Override
    public BigDecimal getStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
               .eq(Transaction::getType, type);

        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }

        List<Transaction> list = transactionMapper.selectList(wrapper);
        return list.stream()
                   .map(Transaction::getAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            return;
        }

        // 回滚账户余额
        Account account = accountMapper.selectById(transaction.getAccountId());
        if (account != null) {
            BigDecimal newBalance;
            // 反向操作：支出加回，收入减去
            if (transaction.getType() == 1) {
                newBalance = account.getBalance().add(transaction.getAmount());
            } else {
                newBalance = account.getBalance().subtract(transaction.getAmount());
            }
            account.setBalance(newBalance);
            accountMapper.updateById(account);
        }

        // 软删除交易记录
        transactionMapper.deleteById(id);
    }

    @Override
    public List<CategoryStatisticsVO> getCategoryStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate) {
        // 1. 查询交易记录
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
               .eq(Transaction::getType, type);

        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }

        List<Transaction> transactions = transactionMapper.selectList(wrapper);

        // 2. 按分类汇总
        Map<Long, BigDecimal> categoryAmountMap = new HashMap<>();
        for (Transaction t : transactions) {
            Long categoryId = t.getCategoryId();
            BigDecimal amount = t.getAmount();
            categoryAmountMap.merge(categoryId, amount, BigDecimal::add);
        }

        // 3. 计算总金额
        BigDecimal totalAmount = categoryAmountMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 构建VO列表
        List<CategoryStatisticsVO> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : categoryAmountMap.entrySet()) {
            CategoryStatisticsVO vo = new CategoryStatisticsVO();
            vo.setCategoryId(entry.getKey());
            vo.setAmount(entry.getValue());

            // 获取分类信息
            Category category = categoryMapper.selectById(entry.getKey());
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryIcon(category.getIcon());
            } else {
                vo.setCategoryName("未分类");
                vo.setCategoryIcon("");
            }

            // 计算占比
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = entry.getValue()
                        .multiply(new BigDecimal("100"))
                        .divide(totalAmount, 2, RoundingMode.HALF_UP);
                vo.setPercentage(percentage);
            } else {
                vo.setPercentage(BigDecimal.ZERO);
            }

            result.add(vo);
        }

        // 5. 按金额降序排序
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        return result;
    }

    @Override
    public List<MonthlyTrendVO> getMonthlyTrend(Long userId, Integer months) {
        if (months == null || months <= 0) {
            months = 6; // 默认近6个月
        }

        List<MonthlyTrendVO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now).minusMonths(i);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            String monthStr = yearMonth.format(formatter);

            // 查询该月收入
            BigDecimal income = getStatistics(userId, 2, startDate, endDate);
            // 查询该月支出
            BigDecimal expense = getStatistics(userId, 1, startDate, endDate);

            MonthlyTrendVO vo = new MonthlyTrendVO();
            vo.setMonth(monthStr);
            vo.setIncome(income);
            vo.setExpense(expense);
            vo.setBalance(income.subtract(expense));

            result.add(vo);
        }

        return result;
    }

    @Override
    public Map<String, Object> getYearlyStatistics(Long userId, Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // 年度总收入
        BigDecimal totalIncome = getStatistics(userId, 2, startDate, endDate);
        // 年度总支出
        BigDecimal totalExpense = getStatistics(userId, 1, startDate, endDate);

        // 月均收入
        BigDecimal avgMonthlyIncome = totalIncome.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        // 月均支出
        BigDecimal avgMonthlyExpense = totalExpense.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

        // 年度结余
        BigDecimal yearlyBalance = totalIncome.subtract(totalExpense);

        // 支出分类统计（Top 5）
        List<CategoryStatisticsVO> expenseByCategory = getCategoryStatistics(userId, 1, startDate, endDate);
        List<CategoryStatisticsVO> top5Expense = expenseByCategory.stream()
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("yearlyBalance", yearlyBalance);
        result.put("avgMonthlyIncome", avgMonthlyIncome);
        result.put("avgMonthlyExpense", avgMonthlyExpense);
        result.put("topExpenseCategories", top5Expense);

        return result;
    }
}
