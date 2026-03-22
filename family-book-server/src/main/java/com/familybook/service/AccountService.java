package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Account;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户服务接口
 */
public interface AccountService extends IService<Account> {

    /**
     * 获取用户账户列表
     */
    List<Account> getUserAccounts(Long userId);

    /**
     * 创建账户
     */
    Account createAccount(Account account);

    /**
     * 更新账户余额（原子操作）
     */
    void updateBalance(Long accountId, BigDecimal amount);

    /**
     * 获取总资产
     */
    BigDecimal getTotalAssets(Long userId);
}
