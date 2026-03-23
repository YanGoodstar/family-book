package com.familybook.service;

import com.familybook.FamilyBookApplication;
import com.familybook.entity.Account;
import com.familybook.entity.Budget;
import com.familybook.entity.Category;
import com.familybook.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BudgetServiceImpl 集成测试
 * 测试预算设置、预算使用计算、预算预警等功能
 * 使用真实Spring上下文和数据库，测试结束后回滚数据
 */
@SpringBootTest(classes = FamilyBookApplication.class)
@Transactional
class BudgetServiceImplTest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    private static final Long TEST_USER_ID = 999999L;
    private static final String TEST_MONTH = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    private Long foodCategoryId;
    private Long transportCategoryId;
    private Long cashAccountId;

    @BeforeEach
    void setUp() {
        // 创建测试用的分类
        Category foodCategory = new Category();
        foodCategory.setUserId(TEST_USER_ID);
        foodCategory.setName("餐饮");
        foodCategory.setType(2); // 支出
        foodCategory.setIcon("food");
        categoryService.save(foodCategory);
        foodCategoryId = foodCategory.getId();

        Category transportCategory = new Category();
        transportCategory.setUserId(TEST_USER_ID);
        transportCategory.setName("交通");
        transportCategory.setType(2); // 支出
        transportCategory.setIcon("transport");
        categoryService.save(transportCategory);
        transportCategoryId = transportCategory.getId();

        // 创建测试用的账户
        Account cashAccount = new Account();
        cashAccount.setUserId(TEST_USER_ID);
        cashAccount.setName("现金");
        cashAccount.setType(1);
        cashAccount.setBalance(new BigDecimal("10000.00"));
        accountService.createAccount(cashAccount);
        cashAccountId = cashAccount.getId();
    }

    @Test
    @DisplayName("设置预算 - 新建总预算")
    void testSetBudget_CreateTotalBudget() {
        // 1. 准备测试数据
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(null); // 总预算
        budget.setBudgetAmount(new BigDecimal("5000.00"));
        budget.setAlertThreshold(new BigDecimal("0.8"));

        // 2. 调用被测方法
        Budget result = budgetService.setBudget(budget);

        // 3. 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_MONTH, result.getBudgetMonth());
        assertNull(result.getCategoryId());
        assertEquals(new BigDecimal("5000.00"), result.getBudgetAmount());
        assertEquals(new BigDecimal("0.8"), result.getAlertThreshold());
        assertEquals(0, result.getIsAlerted());
    }

    @Test
    @DisplayName("设置预算 - 新建分类预算")
    void testSetBudget_CreateCategoryBudget() {
        // 1. 准备测试数据
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("2000.00"));
        budget.setAlertThreshold(new BigDecimal("0.9"));

        // 2. 调用被测方法
        Budget result = budgetService.setBudget(budget);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(foodCategoryId, result.getCategoryId());
        assertEquals(new BigDecimal("2000.00"), result.getBudgetAmount());
    }

    @Test
    @DisplayName("设置预算 - 更新已存在的预算")
    void testSetBudget_UpdateExistingBudget() {
        // 1. 先创建一个预算
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("2000.00"));
        budget.setAlertThreshold(new BigDecimal("0.8"));
        Budget savedBudget = budgetService.setBudget(budget);
        Long budgetId = savedBudget.getId();

        // 2. 更新同一月份同一分类的预算
        Budget updatedBudget = new Budget();
        updatedBudget.setUserId(TEST_USER_ID);
        updatedBudget.setBudgetMonth(TEST_MONTH);
        updatedBudget.setCategoryId(foodCategoryId);
        updatedBudget.setBudgetAmount(new BigDecimal("3000.00"));
        updatedBudget.setAlertThreshold(new BigDecimal("0.9"));

        Budget result = budgetService.setBudget(updatedBudget);

        // 3. 验证结果是更新而非新建
        assertEquals(budgetId, result.getId());
        assertEquals(new BigDecimal("3000.00"), result.getBudgetAmount());
        assertEquals(new BigDecimal("0.9"), result.getAlertThreshold());
    }

    @Test
    @DisplayName("设置预算 - 不同月份可以创建独立预算")
    void testSetBudget_DifferentMonths() {
        String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 创建本月预算
        Budget thisMonthBudget = new Budget();
        thisMonthBudget.setUserId(TEST_USER_ID);
        thisMonthBudget.setBudgetMonth(TEST_MONTH);
        thisMonthBudget.setCategoryId(foodCategoryId);
        thisMonthBudget.setBudgetAmount(new BigDecimal("2000.00"));
        budgetService.setBudget(thisMonthBudget);

        // 创建上月预算
        Budget lastMonthBudget = new Budget();
        lastMonthBudget.setUserId(TEST_USER_ID);
        lastMonthBudget.setBudgetMonth(lastMonth);
        lastMonthBudget.setCategoryId(foodCategoryId);
        lastMonthBudget.setBudgetAmount(new BigDecimal("1500.00"));
        budgetService.setBudget(lastMonthBudget);

        // 验证可以获取两个预算
        List<Budget> thisMonthBudgets = budgetService.getBudgets(TEST_USER_ID, null, TEST_MONTH);
        List<Budget> lastMonthBudgets = budgetService.getBudgets(TEST_USER_ID, null, lastMonth);

        assertEquals(1, thisMonthBudgets.size());
        assertEquals(1, lastMonthBudgets.size());
        assertEquals(new BigDecimal("2000.00"), thisMonthBudgets.get(0).getBudgetAmount());
        assertEquals(new BigDecimal("1500.00"), lastMonthBudgets.get(0).getBudgetAmount());
    }

    @Test
    @DisplayName("获取预算列表 - 按月份查询")
    void testGetBudgets_ByMonth() {
        // 创建多个预算
        Budget totalBudget = new Budget();
        totalBudget.setUserId(TEST_USER_ID);
        totalBudget.setBudgetMonth(TEST_MONTH);
        totalBudget.setCategoryId(null);
        totalBudget.setBudgetAmount(new BigDecimal("5000.00"));
        budgetService.setBudget(totalBudget);

        Budget foodBudget = new Budget();
        foodBudget.setUserId(TEST_USER_ID);
        foodBudget.setBudgetMonth(TEST_MONTH);
        foodBudget.setCategoryId(foodCategoryId);
        foodBudget.setBudgetAmount(new BigDecimal("2000.00"));
        budgetService.setBudget(foodBudget);

        Budget transportBudget = new Budget();
        transportBudget.setUserId(TEST_USER_ID);
        transportBudget.setBudgetMonth(TEST_MONTH);
        transportBudget.setCategoryId(transportCategoryId);
        transportBudget.setBudgetAmount(new BigDecimal("500.00"));
        budgetService.setBudget(transportBudget);

        // 查询预算列表
        List<Budget> budgets = budgetService.getBudgets(TEST_USER_ID, null, TEST_MONTH);

        assertNotNull(budgets);
        assertEquals(3, budgets.size());
    }

    @Test
    @DisplayName("获取预算列表 - 无预算返回空列表")
    void testGetBudgets_NoBudgets() {
        String emptyMonth = "1990-01";
        List<Budget> budgets = budgetService.getBudgets(TEST_USER_ID, null, emptyMonth);

        assertNotNull(budgets);
        assertTrue(budgets.isEmpty());
    }

    @Test
    @DisplayName("获取预算使用 - 总预算使用情况")
    void testGetBudgetUsage_TotalBudget() {
        // 1. 创建总预算
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(null);
        budget.setBudgetAmount(new BigDecimal("5000.00"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 创建支出记录
        LocalDate today = LocalDate.now();
        Transaction expense1 = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("1000.00"), "餐饮", today);
        Transaction expense2 = createTransaction(TEST_USER_ID, cashAccountId, transportCategoryId, new BigDecimal("500.00"), "交通", today);
        transactionService.record(expense1);
        transactionService.record(expense2);

        // 3. 获取预算使用
        BigDecimal usage = budgetService.getBudgetUsage(savedBudget.getId());

        // 4. 验证结果 - 总预算应包含所有分类支出
        assertEquals(new BigDecimal("1500.00"), usage);
    }

    @Test
    @DisplayName("获取预算使用 - 分类预算使用情况")
    void testGetBudgetUsage_CategoryBudget() {
        // 1. 创建分类预算
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("2000.00"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 创建不同分类的支出
        LocalDate today = LocalDate.now();
        Transaction foodExpense = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("800.00"), "餐饮", today);
        Transaction transportExpense = createTransaction(TEST_USER_ID, cashAccountId, transportCategoryId, new BigDecimal("300.00"), "交通", today);
        transactionService.record(foodExpense);
        transactionService.record(transportExpense);

        // 3. 获取预算使用 - 应只包含餐饮分类
        BigDecimal usage = budgetService.getBudgetUsage(savedBudget.getId());

        // 4. 验证结果
        assertEquals(new BigDecimal("800.00"), usage);
    }

    @Test
    @DisplayName("获取预算使用 - 跨月份支出不计入")
    void testGetBudgetUsage_DifferentMonth() {
        // 1. 创建本月预算
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("2000.00"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 创建上月支出
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        Transaction lastMonthExpense = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("500.00"), "上月餐饮", lastMonth);
        transactionService.record(lastMonthExpense);

        // 3. 获取预算使用
        BigDecimal usage = budgetService.getBudgetUsage(savedBudget.getId());

        // 4. 验证结果 - 上月支出不计入本月预算
        assertEquals(0, usage.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("获取预算使用 - 预算不存在返回零")
    void testGetBudgetUsage_BudgetNotFound() {
        BigDecimal usage = budgetService.getBudgetUsage(999999L);
        assertEquals(0, usage.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("检查预算预警 - 未达到阈值不预警")
    void testCheckBudgetAlert_NoAlert() {
        // 1. 创建预算，阈值80%
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("1000.00"));
        budget.setAlertThreshold(new BigDecimal("0.8"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 支出500元（50% < 80%）
        LocalDate today = LocalDate.now();
        Transaction expense = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("500.00"), "餐饮", today);
        transactionService.record(expense);

        // 3. 检查预警
        boolean isAlert = budgetService.checkBudgetAlert(savedBudget.getId());

        // 4. 验证结果
        assertFalse(isAlert);

        // 验证预警状态未更新
        Budget updatedBudget = budgetService.getById(savedBudget.getId());
        assertEquals(0, updatedBudget.getIsAlerted());
    }

    @Test
    @DisplayName("检查预算预警 - 达到阈值触发预警")
    void testCheckBudgetAlert_TriggerAlert() {
        // 1. 创建预算，阈值80%
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("1000.00"));
        budget.setAlertThreshold(new BigDecimal("0.8"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 支出850元（85% > 80%）
        LocalDate today = LocalDate.now();
        Transaction expense = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("850.00"), "餐饮", today);
        transactionService.record(expense);

        // 3. 检查预警
        boolean isAlert = budgetService.checkBudgetAlert(savedBudget.getId());

        // 4. 验证结果
        assertTrue(isAlert);

        // 验证预警状态已更新
        Budget updatedBudget = budgetService.getById(savedBudget.getId());
        assertEquals(1, updatedBudget.getIsAlerted());
    }

    @Test
    @DisplayName("检查预算预警 - 刚好达到阈值触发预警")
    void testCheckBudgetAlert_ExactlyAtThreshold() {
        // 1. 创建预算，阈值80%
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("1000.00"));
        budget.setAlertThreshold(new BigDecimal("0.8"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 支出800元（刚好80%）
        LocalDate today = LocalDate.now();
        Transaction expense = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("800.00"), "餐饮", today);
        transactionService.record(expense);

        // 3. 检查预警
        boolean isAlert = budgetService.checkBudgetAlert(savedBudget.getId());

        // 4. 验证结果 - 刚好达到阈值也应触发
        assertTrue(isAlert);
    }

    @Test
    @DisplayName("检查预算预警 - 超支触发预警")
    void testCheckBudgetAlert_OverBudget() {
        // 1. 创建预算
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(new BigDecimal("1000.00"));
        budget.setAlertThreshold(new BigDecimal("0.8"));
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 支出1200元（超支）
        LocalDate today = LocalDate.now();
        Transaction expense = createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("1200.00"), "餐饮", today);
        transactionService.record(expense);

        // 3. 检查预警
        boolean isAlert = budgetService.checkBudgetAlert(savedBudget.getId());

        // 4. 验证结果
        assertTrue(isAlert);
    }

    @Test
    @DisplayName("检查预算预警 - 预算不存在返回false")
    void testCheckBudgetAlert_BudgetNotFound() {
        boolean isAlert = budgetService.checkBudgetAlert(999999L);
        assertFalse(isAlert);
    }

    @Test
    @DisplayName("检查预算预警 - 预算金额为0返回false")
    void testCheckBudgetAlert_ZeroBudget() {
        // 1. 创建预算金额为0的预算
        Budget budget = new Budget();
        budget.setUserId(TEST_USER_ID);
        budget.setBudgetMonth(TEST_MONTH);
        budget.setCategoryId(foodCategoryId);
        budget.setBudgetAmount(BigDecimal.ZERO);
        Budget savedBudget = budgetService.setBudget(budget);

        // 2. 检查预警
        boolean isAlert = budgetService.checkBudgetAlert(savedBudget.getId());

        // 3. 验证结果
        assertFalse(isAlert);
    }

    @Test
    @DisplayName("综合测试 - 完整的预算管理流程")
    void testCompleteBudgetWorkflow() {
        LocalDate today = LocalDate.now();

        // 1. 设置总预算和分类预算
        Budget totalBudget = new Budget();
        totalBudget.setUserId(TEST_USER_ID);
        totalBudget.setBudgetMonth(TEST_MONTH);
        totalBudget.setCategoryId(null);
        totalBudget.setBudgetAmount(new BigDecimal("5000.00"));
        totalBudget.setAlertThreshold(new BigDecimal("0.8"));
        Budget savedTotalBudget = budgetService.setBudget(totalBudget);

        Budget foodBudget = new Budget();
        foodBudget.setUserId(TEST_USER_ID);
        foodBudget.setBudgetMonth(TEST_MONTH);
        foodBudget.setCategoryId(foodCategoryId);
        foodBudget.setBudgetAmount(new BigDecimal("2000.00"));
        foodBudget.setAlertThreshold(new BigDecimal("0.9"));
        Budget savedFoodBudget = budgetService.setBudget(foodBudget);

        // 2. 记录支出
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("1500.00"), "餐饮", today));
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, transportCategoryId, new BigDecimal("800.00"), "交通", today));

        // 3. 验证预算使用
        BigDecimal totalUsage = budgetService.getBudgetUsage(savedTotalBudget.getId());
        BigDecimal foodUsage = budgetService.getBudgetUsage(savedFoodBudget.getId());

        assertEquals(new BigDecimal("2300.00"), totalUsage); // 1500 + 800
        assertEquals(new BigDecimal("1500.00"), foodUsage); // 仅餐饮

        // 4. 验证预警状态
        // 总预算：2300/5000 = 46% < 80%，不预警
        assertFalse(budgetService.checkBudgetAlert(savedTotalBudget.getId()));

        // 餐饮预算：1500/2000 = 75% < 90%，不预警
        assertFalse(budgetService.checkBudgetAlert(savedFoodBudget.getId()));

        // 5. 继续支出使餐饮预算达到预警阈值
        transactionService.record(createTransaction(TEST_USER_ID, cashAccountId, foodCategoryId, new BigDecimal("500.00"), "聚餐", today));

        // 餐饮预算：2000/2000 = 100% >= 90%，触发预警
        assertTrue(budgetService.checkBudgetAlert(savedFoodBudget.getId()));
    }

    /**
     * 辅助方法：创建交易对象
     */
    private Transaction createTransaction(Long userId, Long accountId, Long categoryId,
                                          BigDecimal amount, String remark, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setCategoryId(categoryId);
        transaction.setType(1); // 支出
        transaction.setAmount(amount);
        transaction.setRemark(remark);
        transaction.setTransactionDate(date);
        return transaction;
    }
}
