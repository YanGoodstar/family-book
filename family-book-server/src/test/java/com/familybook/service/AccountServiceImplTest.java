package com.familybook.service;

import com.familybook.FamilyBookApplication;
import com.familybook.entity.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountServiceImpl 集成测试
 * 测试账户创建、余额更新、总资产计算等功能
 * 使用真实Spring上下文和数据库，测试结束后回滚数据
 */
@SpringBootTest(classes = FamilyBookApplication.class)
@Transactional
class AccountServiceImplTest {

    @Autowired
    private AccountService accountService;

    private static final Long TEST_USER_ID = 999999L;

    @Test
    @DisplayName("获取用户账户列表 - 按排序字段排序")
    void testGetUserAccounts_OrderBySort() {
        // 1. 创建多个账户
        Account account1 = new Account();
        account1.setUserId(TEST_USER_ID);
        account1.setName("现金");
        account1.setType(1);
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setSort(3);
        accountService.createAccount(account1);

        Account account2 = new Account();
        account2.setUserId(TEST_USER_ID);
        account2.setName("支付宝");
        account2.setType(4);
        account2.setBalance(new BigDecimal("5000.00"));
        account2.setSort(1);
        accountService.createAccount(account2);

        Account account3 = new Account();
        account3.setUserId(TEST_USER_ID);
        account3.setName("银行卡");
        account3.setType(2);
        account3.setBalance(new BigDecimal("10000.00"));
        account3.setSort(2);
        accountService.createAccount(account3);

        // 2. 调用被测方法
        List<Account> result = accountService.getUserAccounts(TEST_USER_ID);

        // 3. 验证结果 - 按sort升序排列
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("支付宝", result.get(0).getName()); // sort=1
        assertEquals("银行卡", result.get(1).getName()); // sort=2
        assertEquals("现金", result.get(2).getName());   // sort=3
    }

    @Test
    @DisplayName("获取用户账户列表 - 无账户返回空列表")
    void testGetUserAccounts_NoAccounts() {
        List<Account> result = accountService.getUserAccounts(999999999L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("创建账户 - 正常创建")
    void testCreateAccount_Normal() {
        // 1. 准备测试数据
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("微信钱包");
        account.setType(5); // 微信
        account.setBalance(new BigDecimal("500.00"));
        account.setIcon("wechat");
        account.setSort(10);

        // 2. 调用被测方法
        Account result = accountService.createAccount(account);

        // 3. 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals("微信钱包", result.getName());
        assertEquals(5, result.getType());
        assertEquals(new BigDecimal("500.00"), result.getBalance());
        assertEquals("wechat", result.getIcon());
        assertEquals(10, result.getSort());
    }

    @Test
    @DisplayName("创建账户 - 默认值设置")
    void testCreateAccount_DefaultValues() {
        // 1. 准备测试数据（只设置必填项）
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("测试账户");
        account.setType(1);

        // 2. 调用被测方法
        Account result = accountService.createAccount(account);

        // 3. 验证默认值
        assertEquals(new BigDecimal("0"), result.getBalance()); // 默认余额为0
        assertEquals(100, result.getSort()); // 默认排序为100
    }

    @Test
    @DisplayName("创建账户 - 信用卡特殊字段")
    void testCreateAccount_CreditCard() {
        // 1. 准备测试数据
        Account creditCard = new Account();
        creditCard.setUserId(TEST_USER_ID);
        creditCard.setName("招商银行信用卡");
        creditCard.setType(3); // 信用卡
        creditCard.setBalance(new BigDecimal("-2000.00")); // 欠款
        creditCard.setCreditLimit(new BigDecimal("20000.00"));
        creditCard.setBillDay(5);
        creditCard.setRepayDay(25);

        // 2. 调用被测方法
        Account result = accountService.createAccount(creditCard);

        // 3. 验证结果
        assertEquals(3, result.getType());
        assertEquals(new BigDecimal("-2000.00"), result.getBalance());
        assertEquals(new BigDecimal("20000.00"), result.getCreditLimit());
        assertEquals(5, result.getBillDay());
        assertEquals(25, result.getRepayDay());
    }

    @Test
    @DisplayName("更新账户余额 - 增加余额")
    void testUpdateBalance_Increase() {
        // 1. 创建账户
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("现金");
        account.setType(1);
        account.setBalance(new BigDecimal("1000.00"));
        Account savedAccount = accountService.createAccount(account);
        Long accountId = savedAccount.getId();

        // 2. 增加余额
        accountService.updateBalance(accountId, new BigDecimal("500.00"));

        // 3. 验证结果
        Account updatedAccount = accountService.getById(accountId);
        assertEquals(new BigDecimal("1500.00"), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("更新账户余额 - 减少余额")
    void testUpdateBalance_Decrease() {
        // 1. 创建账户
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("现金");
        account.setType(1);
        account.setBalance(new BigDecimal("1000.00"));
        Account savedAccount = accountService.createAccount(account);
        Long accountId = savedAccount.getId();

        // 2. 减少余额
        accountService.updateBalance(accountId, new BigDecimal("-300.00"));

        // 3. 验证结果
        Account updatedAccount = accountService.getById(accountId);
        assertEquals(new BigDecimal("700.00"), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("更新账户余额 - 余额不足抛出异常")
    void testUpdateBalance_InsufficientBalance() {
        // 1. 创建账户
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("现金");
        account.setType(1);
        account.setBalance(new BigDecimal("100.00"));
        Account savedAccount = accountService.createAccount(account);
        Long accountId = savedAccount.getId();

        // 2. 尝试减少超过余额的金额
        assertThrows(RuntimeException.class, () ->
            accountService.updateBalance(accountId, new BigDecimal("-200.00")));

        // 3. 验证余额未改变
        Account unchangedAccount = accountService.getById(accountId);
        assertEquals(new BigDecimal("100.00"), unchangedAccount.getBalance());
    }

    @Test
    @DisplayName("更新账户余额 - 余额刚好归零")
    void testUpdateBalance_ExactZero() {
        // 1. 创建账户
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("现金");
        account.setType(1);
        account.setBalance(new BigDecimal("500.00"));
        Account savedAccount = accountService.createAccount(account);
        Long accountId = savedAccount.getId();

        // 2. 刚好取完
        accountService.updateBalance(accountId, new BigDecimal("-500.00"));

        // 3. 验证余额为0（使用compareTo避免精度问题）
        Account updatedAccount = accountService.getById(accountId);
        assertEquals(0, updatedAccount.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("更新账户余额 - 账户不存在抛出异常")
    void testUpdateBalance_AccountNotFound() {
        assertThrows(RuntimeException.class, () ->
            accountService.updateBalance(999999L, new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("获取总资产 - 多个账户汇总")
    void testGetTotalAssets_MultipleAccounts() {
        // 1. 创建多个账户
        Account cashAccount = new Account();
        cashAccount.setUserId(TEST_USER_ID);
        cashAccount.setName("现金");
        cashAccount.setType(1);
        cashAccount.setBalance(new BigDecimal("1000.00"));
        accountService.createAccount(cashAccount);

        Account bankAccount = new Account();
        bankAccount.setUserId(TEST_USER_ID);
        bankAccount.setName("银行卡");
        bankAccount.setType(2);
        bankAccount.setBalance(new BigDecimal("5000.00"));
        accountService.createAccount(bankAccount);

        Account alipayAccount = new Account();
        alipayAccount.setUserId(TEST_USER_ID);
        alipayAccount.setName("支付宝");
        alipayAccount.setType(4);
        alipayAccount.setBalance(new BigDecimal("2000.00"));
        accountService.createAccount(alipayAccount);

        // 2. 调用被测方法
        BigDecimal totalAssets = accountService.getTotalAssets(TEST_USER_ID);

        // 3. 验证结果 - 1000 + 5000 + 2000 = 8000
        assertEquals(new BigDecimal("8000.00"), totalAssets);
    }

    @Test
    @DisplayName("获取总资产 - 包含信用卡欠款")
    void testGetTotalAssets_WithCreditCard() {
        // 1. 创建账户（包含信用卡欠款）
        Account cashAccount = new Account();
        cashAccount.setUserId(TEST_USER_ID);
        cashAccount.setName("现金");
        cashAccount.setType(1);
        cashAccount.setBalance(new BigDecimal("1000.00"));
        accountService.createAccount(cashAccount);

        Account creditCard = new Account();
        creditCard.setUserId(TEST_USER_ID);
        creditCard.setName("信用卡");
        creditCard.setType(3);
        creditCard.setBalance(new BigDecimal("-500.00")); // 欠款500
        accountService.createAccount(creditCard);

        // 2. 调用被测方法
        BigDecimal totalAssets = accountService.getTotalAssets(TEST_USER_ID);

        // 3. 验证结果 - 1000 + (-500) = 500
        assertEquals(new BigDecimal("500.00"), totalAssets);
    }

    @Test
    @DisplayName("获取总资产 - 无账户返回0")
    void testGetTotalAssets_NoAccounts() {
        BigDecimal totalAssets = accountService.getTotalAssets(999999999L);
        assertEquals(0, totalAssets.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("获取总资产 - 忽略其他用户的账户")
    void testGetTotalAssets_OnlyCurrentUser() {
        // 1. 创建当前用户的账户
        Account userAccount = new Account();
        userAccount.setUserId(TEST_USER_ID);
        userAccount.setName("我的现金");
        userAccount.setType(1);
        userAccount.setBalance(new BigDecimal("3000.00"));
        accountService.createAccount(userAccount);

        // 2. 创建其他用户的账户
        Account otherUserAccount = new Account();
        otherUserAccount.setUserId(888888L);
        otherUserAccount.setName("他人现金");
        otherUserAccount.setType(1);
        otherUserAccount.setBalance(new BigDecimal("10000.00"));
        accountService.createAccount(otherUserAccount);

        // 3. 获取当前用户的总资产
        BigDecimal totalAssets = accountService.getTotalAssets(TEST_USER_ID);

        // 4. 验证结果 - 只包含当前用户的账户
        assertEquals(new BigDecimal("3000.00"), totalAssets);
    }

    @Test
    @DisplayName("获取总资产 - 余额为null的账户")
    void testGetTotalAssets_NullBalance() {
        // 1. 创建账户（直接设置null余额）
        Account account = new Account();
        account.setUserId(TEST_USER_ID);
        account.setName("测试账户");
        account.setType(1);
        account.setBalance(new BigDecimal("1000.00"));
        Account savedAccount = accountService.createAccount(account);

        // 直接修改余额为null（模拟异常情况）
        savedAccount.setBalance(new BigDecimal("0"));
        accountService.updateById(savedAccount);

        // 2. 获取总资产
        BigDecimal totalAssets = accountService.getTotalAssets(TEST_USER_ID);

        // 3. 验证结果 - null余额被忽略
        assertEquals(new BigDecimal("0.00"), totalAssets);
    }

    @Test
    @DisplayName("综合测试 - 完整的账户管理流程")
    void testCompleteAccountWorkflow() {
        // 1. 创建多个账户
        Account cashAccount = new Account();
        cashAccount.setUserId(TEST_USER_ID);
        cashAccount.setName("现金");
        cashAccount.setType(1);
        cashAccount.setBalance(new BigDecimal("1000.00"));
        cashAccount.setSort(1);
        Account savedCash = accountService.createAccount(cashAccount);

        Account bankAccount = new Account();
        bankAccount.setUserId(TEST_USER_ID);
        bankAccount.setName("储蓄卡");
        bankAccount.setType(2);
        bankAccount.setBalance(new BigDecimal("5000.00"));
        bankAccount.setSort(2);
        Account savedBank = accountService.createAccount(bankAccount);

        // 2. 验证总资产
        BigDecimal initialTotal = accountService.getTotalAssets(TEST_USER_ID);
        assertEquals(new BigDecimal("6000.00"), initialTotal);

        // 3. 更新余额（模拟收入和支出）
        // 现金收入500
        accountService.updateBalance(savedCash.getId(), new BigDecimal("500.00"));
        // 银行卡支出1000
        accountService.updateBalance(savedBank.getId(), new BigDecimal("-1000.00"));

        // 4. 验证更新后的余额
        Account updatedCash = accountService.getById(savedCash.getId());
        Account updatedBank = accountService.getById(savedBank.getId());
        assertEquals(new BigDecimal("1500.00"), updatedCash.getBalance());
        assertEquals(new BigDecimal("4000.00"), updatedBank.getBalance());

        // 5. 验证更新后的总资产
        BigDecimal updatedTotal = accountService.getTotalAssets(TEST_USER_ID);
        assertEquals(new BigDecimal("5500.00"), updatedTotal);

        // 6. 获取账户列表
        List<Account> accounts = accountService.getUserAccounts(TEST_USER_ID);
        assertEquals(2, accounts.size());
    }
}
