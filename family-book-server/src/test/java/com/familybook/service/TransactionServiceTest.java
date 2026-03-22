package com.familybook.service;

import com.familybook.entity.Account;
import com.familybook.entity.Transaction;
import com.familybook.mapper.AccountMapper;
import com.familybook.mapper.TransactionMapper;
import com.familybook.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TransactionService 单元测试
 * 测试记账、查询、统计、删除等核心功能
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction expenseTransaction;
    private Transaction incomeTransaction;
    private Account account;
    private static final Long USER_ID = 1L;
    private static final Long ACCOUNT_ID = 100L;
    private static final Long FAMILY_ID = 200L;
    private static final Long CATEGORY_ID = 10L;

    @BeforeEach
    void setUp() {
        // 初始化账户
        account = new Account();
        account.setId(ACCOUNT_ID);
        account.setUserId(USER_ID);
        account.setName("测试账户");
        account.setBalance(new BigDecimal("1000.00"));
        account.setType(1);

        // 初始化支出交易
        expenseTransaction = new Transaction();
        expenseTransaction.setId(1L);
        expenseTransaction.setUserId(USER_ID);
        expenseTransaction.setAccountId(ACCOUNT_ID);
        expenseTransaction.setCategoryId(CATEGORY_ID);
        expenseTransaction.setType(1); // 1=支出
        expenseTransaction.setAmount(new BigDecimal("100.00"));
        expenseTransaction.setRemark("午餐");
        expenseTransaction.setTransactionDate(LocalDate.now());
        expenseTransaction.setCreateTime(LocalDateTime.now());
        expenseTransaction.setStatus(1);

        // 初始化收入交易
        incomeTransaction = new Transaction();
        incomeTransaction.setId(2L);
        incomeTransaction.setUserId(USER_ID);
        incomeTransaction.setAccountId(ACCOUNT_ID);
        incomeTransaction.setCategoryId(CATEGORY_ID);
        incomeTransaction.setType(2); // 2=收入
        incomeTransaction.setAmount(new BigDecimal("5000.00"));
        incomeTransaction.setRemark("工资");
        incomeTransaction.setTransactionDate(LocalDate.now());
        incomeTransaction.setCreateTime(LocalDateTime.now());
        incomeTransaction.setStatus(1);
    }

    @Test
    @DisplayName("正常记账 - 支出类型，应减少账户余额")
    void record_ExpenseTransaction_ShouldDecreaseBalance() {
        // Given
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return 1;
        });

        // When
        Transaction result = transactionService.record(expenseTransaction);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(transactionMapper, times(1)).insert(expenseTransaction);
        verify(accountMapper, times(1)).selectById(ACCOUNT_ID);
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("900.00")) == 0
        ));
    }

    @Test
    @DisplayName("正常记账 - 收入类型，应增加账户余额")
    void record_IncomeTransaction_ShouldIncreaseBalance() {
        // Given
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(2L);
            return 1;
        });

        // When
        Transaction result = transactionService.record(incomeTransaction);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(transactionMapper, times(1)).insert(incomeTransaction);
        verify(accountMapper, times(1)).selectById(ACCOUNT_ID);
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("6000.00")) == 0
        ));
    }

    @Test
    @DisplayName("记账 - 账户不存在，应只保存交易不更新余额")
    void record_AccountNotFound_ShouldOnlySaveTransaction() {
        // Given
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(null);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return 1;
        });

        // When
        Transaction result = transactionService.record(expenseTransaction);

        // Then
        assertNotNull(result);
        verify(transactionMapper, times(1)).insert(expenseTransaction);
        verify(accountMapper, times(1)).selectById(ACCOUNT_ID);
        verify(accountMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("记账 - 大额支出，余额应正确计算")
    void record_LargeExpense_ShouldCalculateBalanceCorrectly() {
        // Given
        Transaction largeExpense = new Transaction();
        largeExpense.setUserId(USER_ID);
        largeExpense.setAccountId(ACCOUNT_ID);
        largeExpense.setType(1); // 支出
        largeExpense.setAmount(new BigDecimal("999.99"));

        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(3L);
            return 1;
        });

        // When
        Transaction result = transactionService.record(largeExpense);

        // Then
        assertNotNull(result);
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("0.01")) == 0
        ));
    }

    @Test
    @DisplayName("查询交易列表 - 根据用户ID查询")
    void getTransactions_ByUserId_ShouldReturnTransactions() {
        // Given
        List<Transaction> transactions = Arrays.asList(expenseTransaction, incomeTransaction);
        when(transactionMapper.selectList(any())).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.getTransactions(
                USER_ID, null, null, null, null, null
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("查询交易列表 - 根据类型筛选")
    void getTransactions_ByType_ShouldReturnFilteredTransactions() {
        // Given
        when(transactionMapper.selectList(any())).thenReturn(Collections.singletonList(expenseTransaction));

        // When
        List<Transaction> result = transactionService.getTransactions(
                USER_ID, null, null, null, 1, null
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getType());
    }

    @Test
    @DisplayName("查询交易列表 - 根据日期范围筛选")
    void getTransactions_ByDateRange_ShouldReturnFilteredTransactions() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(transactionMapper.selectList(any())).thenReturn(Arrays.asList(expenseTransaction, incomeTransaction));

        // When
        List<Transaction> result = transactionService.getTransactions(
                USER_ID, null, startDate, endDate, null, null
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("查询交易列表 - 根据分类筛选")
    void getTransactions_ByCategory_ShouldReturnFilteredTransactions() {
        // Given
        when(transactionMapper.selectList(any())).thenReturn(Collections.singletonList(expenseTransaction));

        // When
        List<Transaction> result = transactionService.getTransactions(
                USER_ID, null, null, null, null, CATEGORY_ID
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CATEGORY_ID, result.get(0).getCategoryId());
    }

    @Test
    @DisplayName("查询交易列表 - 无结果返回空列表")
    void getTransactions_NoResults_ShouldReturnEmptyList() {
        // Given
        when(transactionMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When
        List<Transaction> result = transactionService.getTransactions(
                999L, null, null, null, null, null
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("收支统计 - 计算收入总额")
    void getStatistics_IncomeType_ShouldReturnTotalIncome() {
        // Given
        Transaction income1 = new Transaction();
        income1.setAmount(new BigDecimal("5000.00"));
        income1.setType(2);

        Transaction income2 = new Transaction();
        income2.setAmount(new BigDecimal("3000.00"));
        income2.setType(2);

        when(transactionMapper.selectList(any())).thenReturn(Arrays.asList(income1, income2));

        // When
        BigDecimal result = transactionService.getStatistics(USER_ID, 2, null, null);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("8000.00"), result);
    }

    @Test
    @DisplayName("收支统计 - 计算支出总额")
    void getStatistics_ExpenseType_ShouldReturnTotalExpense() {
        // Given
        Transaction expense1 = new Transaction();
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setType(1);

        Transaction expense2 = new Transaction();
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setType(1);

        Transaction expense3 = new Transaction();
        expense3.setAmount(new BigDecimal("50.00"));
        expense3.setType(1);

        when(transactionMapper.selectList(any())).thenReturn(Arrays.asList(expense1, expense2, expense3));

        // When
        BigDecimal result = transactionService.getStatistics(USER_ID, 1, null, null);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("350.00"), result);
    }

    @Test
    @DisplayName("收支统计 - 无记录返回零")
    void getStatistics_NoTransactions_ShouldReturnZero() {
        // Given
        when(transactionMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When
        BigDecimal result = transactionService.getStatistics(USER_ID, 1, null, null);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("收支统计 - 带日期范围")
    void getStatistics_WithDateRange_ShouldReturnFilteredTotal() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        Transaction expense = new Transaction();
        expense.setAmount(new BigDecimal("500.00"));
        expense.setType(1);

        when(transactionMapper.selectList(any())).thenReturn(Collections.singletonList(expense));

        // When
        BigDecimal result = transactionService.getStatistics(USER_ID, 1, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result);
    }

    @Test
    @DisplayName("删除交易 - 支出类型应回滚余额（加回金额）")
    void deleteTransaction_ExpenseType_ShouldRollbackBalance() {
        // Given
        account.setBalance(new BigDecimal("900.00")); // 支出后的余额
        when(transactionMapper.selectById(1L)).thenReturn(expenseTransaction);
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);

        // When
        transactionService.deleteTransaction(1L);

        // Then
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("1000.00")) == 0
        ));
        verify(transactionMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除交易 - 收入类型应回滚余额（减去金额）")
    void deleteTransaction_IncomeType_ShouldRollbackBalance() {
        // Given
        account.setBalance(new BigDecimal("6000.00")); // 收入后的余额
        when(transactionMapper.selectById(2L)).thenReturn(incomeTransaction);
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);

        // When
        transactionService.deleteTransaction(2L);

        // Then
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("1000.00")) == 0
        ));
        verify(transactionMapper, times(1)).deleteById(2L);
    }

    @Test
    @DisplayName("删除交易 - 交易不存在应直接返回")
    void deleteTransaction_TransactionNotFound_ShouldReturnWithoutAction() {
        // Given
        when(transactionMapper.selectById(999L)).thenReturn(null);

        // When
        transactionService.deleteTransaction(999L);

        // Then
        verify(transactionMapper, times(1)).selectById(999L);
        verify(accountMapper, never()).selectById(any());
        verify(accountMapper, never()).updateById(any());
        verify(transactionMapper, never()).deleteById((Serializable) any());
    }

    @Test
    @DisplayName("删除交易 - 账户不存在应只删除交易")
    void deleteTransaction_AccountNotFound_ShouldOnlyDeleteTransaction() {
        // Given
        when(transactionMapper.selectById(1L)).thenReturn(expenseTransaction);
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(null);

        // When
        transactionService.deleteTransaction(1L);

        // Then
        verify(transactionMapper, times(1)).deleteById(1L);
        verify(accountMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除交易 - 小数金额回滚精度测试")
    void deleteTransaction_DecimalAmount_ShouldRollbackWithPrecision() {
        // Given
        Transaction decimalTransaction = new Transaction();
        decimalTransaction.setId(3L);
        decimalTransaction.setUserId(USER_ID);
        decimalTransaction.setAccountId(ACCOUNT_ID);
        decimalTransaction.setType(1); // 支出
        decimalTransaction.setAmount(new BigDecimal("99.99"));

        account.setBalance(new BigDecimal("900.01"));
        when(transactionMapper.selectById(3L)).thenReturn(decimalTransaction);
        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);

        // When
        transactionService.deleteTransaction(3L);

        // Then
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("1000.00")) == 0
        ));
    }

    @Test
    @DisplayName("边界情况 - 支出金额等于余额（余额归零）")
    void record_ExpenseEqualsBalance_ShouldResultInZeroBalance() {
        // Given
        Transaction fullExpense = new Transaction();
        fullExpense.setUserId(USER_ID);
        fullExpense.setAccountId(ACCOUNT_ID);
        fullExpense.setType(1);
        fullExpense.setAmount(new BigDecimal("1000.00"));

        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(4L);
            return 1;
        });

        // When
        Transaction result = transactionService.record(fullExpense);

        // Then
        assertNotNull(result);
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    @DisplayName("边界情况 - 零金额交易")
    void record_ZeroAmount_ShouldNotChangeBalance() {
        // Given
        Transaction zeroTransaction = new Transaction();
        zeroTransaction.setUserId(USER_ID);
        zeroTransaction.setAccountId(ACCOUNT_ID);
        zeroTransaction.setType(1);
        zeroTransaction.setAmount(BigDecimal.ZERO);

        when(accountMapper.selectById(ACCOUNT_ID)).thenReturn(account);
        when(transactionMapper.insert(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(5L);
            return 1;
        });

        // When
        Transaction result = transactionService.record(zeroTransaction);

        // Then
        assertNotNull(result);
        verify(accountMapper, times(1)).updateById(argThat(acc ->
                acc.getBalance().compareTo(new BigDecimal("1000.00")) == 0
        ));
    }
}
