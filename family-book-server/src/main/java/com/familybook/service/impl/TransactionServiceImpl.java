package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Account;
import com.familybook.entity.Transaction;
import com.familybook.mapper.AccountMapper;
import com.familybook.mapper.TransactionMapper;
import com.familybook.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final AccountMapper accountMapper;

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
}
