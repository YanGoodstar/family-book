package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Account;
import com.familybook.mapper.AccountMapper;
import com.familybook.service.AccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户服务实现类
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    private final AccountMapper accountMapper;

    public AccountServiceImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public List<Account> getUserAccounts(Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .orderByAsc(Account::getSort);
        return this.list(wrapper);
    }

    @Override
    public Account createAccount(Account account) {
        // 设置默认排序
        if (account.getSort() == null) {
            account.setSort(100);
        }
        // 设置余额（如果为null）
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        this.save(account);
        return account;
    }

    @Override
    public void updateBalance(Long accountId, BigDecimal amount) {
        Account account = this.getById(accountId);
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }

        // 原子更新余额
        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("账户余额不足");
        }

        account.setBalance(newBalance);
        this.updateById(account);
    }

    @Override
    public BigDecimal getTotalAssets(Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId);

        List<Account> accounts = this.list(wrapper);
        BigDecimal total = BigDecimal.ZERO;

        for (Account account : accounts) {
            if (account.getBalance() != null) {
                total = total.add(account.getBalance());
            }
        }

        return total;
    }
}
