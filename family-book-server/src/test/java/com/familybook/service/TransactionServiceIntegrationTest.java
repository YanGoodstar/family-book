package com.familybook.service;

import com.familybook.FamilyBookApplication;
import com.familybook.entity.Account;
import com.familybook.entity.Category;
import com.familybook.entity.Transaction;
import com.familybook.vo.CategoryStatisticsVO;
import com.familybook.vo.MonthlyTrendVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TransactionService 集成测试
 * 使用 Spring Boot 测试框架和真实数据库
 */
@SpringBootTest(classes = FamilyBookApplication.class)
@Transactional
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    private static final Long TEST_USER_ID = 999999L;
    private Long foodCategoryId;
    private Long salaryCategoryId;
    private Long cashAccountId;

    @BeforeEach
    void setUp() {
        // 创建支出分类
        Category foodCategory = new Category();
        foodCategory.setUserId(TEST_USER_ID);
        foodCategory.setName("餐饮");
        foodCategory.setType(2); // 支出
        foodCategory.setIcon("food");
        categoryService.save(foodCategory);
        foodCategoryId = foodCategory.getId();

        // 创建收入分类
        Category salaryCategory = new Category();
        salaryCategory.setUserId(TEST_USER_ID);
        salaryCategory.setName("工资");
        salaryCategory.setType(1); // 收入
        salaryCategory.setIcon("salary");
        categoryService.save(salaryCategory);
        salaryCategoryId = salaryCategory.getId();

        // 创建账户
        Account cashAccount = new Account();
        cashAccount.setUserId(TEST_USER_ID);
        cashAccount.setName("现金");
        cashAccount.setType(1);
        cashAccount.setBalance(new BigDecimal("10000.00"));
        accountService.createAccount(cashAccount);
        cashAccountId = cashAccount.getId();
    }

    @Test
    @DisplayName("记账 - 支出记录")
    void testRecord_Expense() {
        Transaction transaction = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("100.00"), "午餐", LocalDate.now());

        Transaction result = transactionService.record(transaction);

        assertNotNull(result.getId());
        assertEquals(new BigDecimal("100.00"), result.getAmount());

        // 验证账户余额减少
        Account account = accountService.getById(cashAccountId);
        assertEquals(new BigDecimal("9900.00"), account.getBalance());
    }

    @Test
    @DisplayName("记账 - 收入记录")
    void testRecord_Income() {
        Transaction transaction = createTransaction(TEST_USER_ID, cashAccountId, salaryCategoryId,
                2, new BigDecimal("5000.00"), "工资", LocalDate.now());

        Transaction result = transactionService.record(transaction);

        assertNotNull(result.getId());

        // 验证账户余额增加
        Account account = accountService.getById(cashAccountId);
        assertEquals(new BigDecimal("15000.00"), account.getBalance());
    }

    @Test
    @DisplayName("获取交易列表 - 按用户查询")
    void testGetTransactions_ByUser() {
        // 创建交易记录
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("100.00"), "餐饮", LocalDate.now()));

        List<Transaction> result = transactionService.getTransactions(
                TEST_USER_ID, null, null, null, null, null);

        assertFalse(result.isEmpty());
        assertEquals(TEST_USER_ID, result.get(0).getUserId());
    }

    @Test
    @DisplayName("获取交易列表 - 按日期范围查询")
    void testGetTransactions_ByDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("100.00"), "今天", today));

        List<Transaction> result = transactionService.getTransactions(
                TEST_USER_ID, null, yesterday, tomorrow, null, null);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取交易列表 - 按类型查询")
    void testGetTransactions_ByType() {
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("100.00"), "支出", LocalDate.now()));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, salaryCategoryId,
                2, new BigDecimal("5000.00"), "收入", LocalDate.now()));

        List<Transaction> expenses = transactionService.getTransactions(
                TEST_USER_ID, null, null, null, 1, null);
        List<Transaction> incomes = transactionService.getTransactions(
                TEST_USER_ID, null, null, null, 2, null);

        assertEquals(1, expenses.size());
        assertEquals(1, incomes.size());
    }

    @Test
    @DisplayName("收支统计 - 计算总收入")
    void testGetStatistics_TotalIncome() {
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, salaryCategoryId,
                2, new BigDecimal("5000.00"), "工资", LocalDate.now()));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, salaryCategoryId,
                2, new BigDecimal("2000.00"), "奖金", LocalDate.now()));

        BigDecimal result = transactionService.getStatistics(TEST_USER_ID, 2,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertEquals(new BigDecimal("7000.00"), result);
    }

    @Test
    @DisplayName("收支统计 - 计算总支出")
    void testGetStatistics_TotalExpense() {
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("100.00"), "早餐", LocalDate.now()));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("200.00"), "午餐", LocalDate.now()));

        BigDecimal result = transactionService.getStatistics(TEST_USER_ID, 1,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertEquals(new BigDecimal("300.00"), result);
    }

    @Test
    @DisplayName("删除交易 - 回滚账户余额")
    void testDeleteTransaction_RollbackBalance() {
        Transaction transaction = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("500.00"), "支出", LocalDate.now());
        Transaction saved = transactionService.record(transaction);

        // 删除前余额
        Account beforeDelete = accountService.getById(cashAccountId);
        assertEquals(new BigDecimal("9500.00"), beforeDelete.getBalance());

        transactionService.deleteTransaction(saved.getId());

        // 删除后余额恢复
        Account afterDelete = accountService.getById(cashAccountId);
        assertEquals(new BigDecimal("10000.00"), afterDelete.getBalance());
    }

    @Test
    @DisplayName("分类统计 - 按支出分类统计")
    void testGetCategoryStatistics_Expense() {
        // 创建多个支出
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("500.00"), "餐饮1", LocalDate.now()));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("300.00"), "餐饮2", LocalDate.now()));

        List<CategoryStatisticsVO> result = transactionService.getCategoryStatistics(
                TEST_USER_ID, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertFalse(result.isEmpty());
        assertEquals(foodCategoryId, result.get(0).getCategoryId());
        assertEquals(new BigDecimal("800.00"), result.get(0).getAmount());
        assertEquals(new BigDecimal("100.00"), result.get(0).getPercentage()); // 占比100%
    }

    @Test
    @DisplayName("月度趋势 - 近6个月收支趋势")
    void testGetMonthlyTrend_Default6Months() {
        List<MonthlyTrendVO> result = transactionService.getMonthlyTrend(TEST_USER_ID, 6);

        assertEquals(6, result.size());
        assertNotNull(result.get(0).getMonth());
        assertNotNull(result.get(0).getIncome());
        assertNotNull(result.get(0).getExpense());
        assertNotNull(result.get(0).getBalance());
    }

    @Test
    @DisplayName("月度趋势 - 指定月份数")
    void testGetMonthlyTrend_CustomMonths() {
        List<MonthlyTrendVO> result = transactionService.getMonthlyTrend(TEST_USER_ID, 3);

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("年度统计 - 汇总年度数据")
    void testGetYearlyStatistics() {
        // 创建本年度交易
        LocalDate today = LocalDate.now();
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, salaryCategoryId,
                2, new BigDecimal("10000.00"), "工资", today));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("3000.00"), "支出", today));

        Map<String, Object> result = transactionService.getYearlyStatistics(TEST_USER_ID, today.getYear());

        assertEquals(today.getYear(), result.get("year"));
        assertEquals(new BigDecimal("10000.00"), result.get("totalIncome"));
        assertEquals(new BigDecimal("3000.00"), result.get("totalExpense"));
        assertEquals(new BigDecimal("7000.00"), result.get("yearlyBalance"));
        assertNotNull(result.get("avgMonthlyIncome"));
        assertNotNull(result.get("avgMonthlyExpense"));
        assertNotNull(result.get("topExpenseCategories"));
    }

    @Test
    @DisplayName("综合测试 - 完整记账流程")
    void testCompleteWorkflow() {
        LocalDate today = LocalDate.now();

        // 1. 记录收入
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, salaryCategoryId,
                2, new BigDecimal("10000.00"), "工资", today));

        // 2. 记录多笔支出
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("200.00"), "午餐", today));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId,
                1, new BigDecimal("150.00"), "晚餐", today));

        // 3. 验证统计
        BigDecimal income = transactionService.getStatistics(TEST_USER_ID, 2,
                today.minusDays(1), today.plusDays(1));
        BigDecimal expense = transactionService.getStatistics(TEST_USER_ID, 1,
                today.minusDays(1), today.plusDays(1));

        assertEquals(new BigDecimal("10000.00"), income);
        assertEquals(new BigDecimal("350.00"), expense);

        // 4. 验证账户余额
        Account account = accountService.getById(cashAccountId);
        assertEquals(new BigDecimal("19650.00"), account.getBalance()); // 10000 + 10000 - 350
    }

    private Transaction createTransaction(Long userId, Long accountId, Long categoryId,
                                          Integer type, BigDecimal amount, String remark, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setCategoryId(categoryId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setRemark(remark);
        transaction.setTransactionDate(date);
        return transaction;
    }
}
