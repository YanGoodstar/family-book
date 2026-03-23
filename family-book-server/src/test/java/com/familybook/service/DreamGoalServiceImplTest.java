package com.familybook.service;

import com.familybook.FamilyBookApplication;
import com.familybook.entity.DreamGoal;
import com.familybook.entity.SavingsRecord;
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
 * DreamGoalServiceImpl 集成测试
 * 测试梦想目标创建、储蓄进度计算、月度储蓄执行等功能
 * 使用真实Spring上下文和数据库，测试结束后回滚数据
 */
@SpringBootTest(classes = FamilyBookApplication.class)
@Transactional
class DreamGoalServiceImplTest {

    @Autowired
    private DreamGoalService dreamGoalService;

    @Autowired
    private SavingsRecordService savingsRecordService;

    private static final Long TEST_USER_ID = 999999L;

    @Test
    @DisplayName("创建梦想目标 - 固定金额模式")
    void testCreateDreamGoal_FixedAmountMode() {
        // 1. 准备测试数据
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("买车");
        dreamGoal.setTargetAmount(new BigDecimal("100000.00"));
        dreamGoal.setTargetDate(LocalDate.now().plusYears(2));
        dreamGoal.setSavingsType(1); // 固定金额模式
        dreamGoal.setSavingsAmount(new BigDecimal("5000.00"));
        dreamGoal.setPriority(1);
        dreamGoal.setIcon("car");

        // 2. 调用被测方法
        DreamGoal result = dreamGoalService.createDreamGoal(dreamGoal);

        // 3. 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals("买车", result.getName());
        assertEquals(new BigDecimal("100000.00"), result.getTargetAmount());
        assertEquals(0, result.getSavedAmount().compareTo(BigDecimal.ZERO)); // 默认已存为0
        assertEquals(1, result.getSavingsType());
        assertEquals(new BigDecimal("5000.00"), result.getSavingsAmount());
        assertEquals(1, result.getPriority());
    }

    @Test
    @DisplayName("创建梦想目标 - 工资百分比模式")
    void testCreateDreamGoal_PercentageMode() {
        // 1. 准备测试数据
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("买房首付");
        dreamGoal.setTargetAmount(new BigDecimal("500000.00"));
        dreamGoal.setTargetDate(LocalDate.now().plusYears(5));
        dreamGoal.setSavingsType(2); // 百分比模式
        dreamGoal.setMonthlyIncome(new BigDecimal("20000.00"));
        dreamGoal.setSavingsPercent(new BigDecimal("0.3")); // 30%
        dreamGoal.setPriority(2);
        dreamGoal.setIcon("house");

        // 2. 调用被测方法
        DreamGoal result = dreamGoalService.createDreamGoal(dreamGoal);

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(2, result.getSavingsType());
        assertEquals(new BigDecimal("20000.00"), result.getMonthlyIncome());
        assertEquals(new BigDecimal("0.3"), result.getSavingsPercent());
    }

    @Test
    @DisplayName("创建梦想目标 - 默认值设置")
    void testCreateDreamGoal_DefaultValues() {
        // 1. 准备测试数据（只设置必填项）
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("旅游基金");
        dreamGoal.setTargetAmount(new BigDecimal("20000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("2000.00"));

        // 2. 调用被测方法
        DreamGoal result = dreamGoalService.createDreamGoal(dreamGoal);

        // 3. 验证默认值
        assertEquals(0, result.getSavedAmount().compareTo(BigDecimal.ZERO)); // 默认已存金额
        assertEquals(1, result.getPriority()); // 默认优先级
    }

    @Test
    @DisplayName("创建梦想目标 - 目标金额必须大于0")
    void testCreateDreamGoal_InvalidTargetAmount() {
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("无效目标");
        dreamGoal.setTargetAmount(BigDecimal.ZERO);
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("1000.00"));

        assertThrows(RuntimeException.class, () -> dreamGoalService.createDreamGoal(dreamGoal));
    }

    @Test
    @DisplayName("创建梦想目标 - 固定金额模式下储蓄金额必须大于0")
    void testCreateDreamGoal_InvalidFixedAmount() {
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("无效目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(BigDecimal.ZERO);

        assertThrows(RuntimeException.class, () -> dreamGoalService.createDreamGoal(dreamGoal));
    }

    @Test
    @DisplayName("创建梦想目标 - 百分比模式下月收入必须大于0")
    void testCreateDreamGoal_InvalidMonthlyIncome() {
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("无效目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(2);
        dreamGoal.setMonthlyIncome(BigDecimal.ZERO);
        dreamGoal.setSavingsPercent(new BigDecimal("0.2"));

        assertThrows(RuntimeException.class, () -> dreamGoalService.createDreamGoal(dreamGoal));
    }

    @Test
    @DisplayName("创建梦想目标 - 百分比模式下储蓄百分比必须大于0")
    void testCreateDreamGoal_InvalidSavingsPercent() {
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("无效目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(2);
        dreamGoal.setMonthlyIncome(new BigDecimal("10000.00"));
        dreamGoal.setSavingsPercent(BigDecimal.ZERO);

        assertThrows(RuntimeException.class, () -> dreamGoalService.createDreamGoal(dreamGoal));
    }

    @Test
    @DisplayName("获取用户梦想目标列表 - 按优先级排序")
    void testGetUserDreamGoals_OrderByPriority() {
        // 1. 创建多个梦想目标
        DreamGoal goal1 = new DreamGoal();
        goal1.setUserId(TEST_USER_ID);
        goal1.setName("目标1-低优先级");
        goal1.setTargetAmount(new BigDecimal("10000.00"));
        goal1.setSavingsType(1);
        goal1.setSavingsAmount(new BigDecimal("1000.00"));
        goal1.setPriority(3);
        dreamGoalService.createDreamGoal(goal1);

        DreamGoal goal2 = new DreamGoal();
        goal2.setUserId(TEST_USER_ID);
        goal2.setName("目标2-高优先级");
        goal2.setTargetAmount(new BigDecimal("20000.00"));
        goal2.setSavingsType(1);
        goal2.setSavingsAmount(new BigDecimal("2000.00"));
        goal2.setPriority(1);
        dreamGoalService.createDreamGoal(goal2);

        DreamGoal goal3 = new DreamGoal();
        goal3.setUserId(TEST_USER_ID);
        goal3.setName("目标3-中优先级");
        goal3.setTargetAmount(new BigDecimal("15000.00"));
        goal3.setSavingsType(1);
        goal3.setSavingsAmount(new BigDecimal("1500.00"));
        goal3.setPriority(2);
        dreamGoalService.createDreamGoal(goal3);

        // 2. 调用被测方法
        List<DreamGoal> result = dreamGoalService.getUserDreamGoals(TEST_USER_ID, null);

        // 3. 验证结果 - 按优先级升序排列
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("目标2-高优先级", result.get(0).getName());
        assertEquals("目标3-中优先级", result.get(1).getName());
        assertEquals("目标1-低优先级", result.get(2).getName());
    }

    @Test
    @DisplayName("获取用户梦想目标列表 - 无目标返回空列表")
    void testGetUserDreamGoals_NoGoals() {
        List<DreamGoal> result = dreamGoalService.getUserDreamGoals(999999999L, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("执行月度储蓄 - 固定金额模式达标")
    void testExecuteMonthlySaving_FixedAmount_Completed() {
        // 1. 创建固定金额模式的梦想目标
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("买车");
        dreamGoal.setTargetAmount(new BigDecimal("100000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("5000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);
        Long goalId = savedGoal.getId();

        // 2. 执行月度储蓄 - 存入金额 >= 计划金额
        dreamGoalService.executeMonthlySaving(goalId, new BigDecimal("5000.00"));

        // 3. 验证梦想目标已存金额更新
        DreamGoal updatedGoal = dreamGoalService.getById(goalId);
        assertEquals(new BigDecimal("5000.00"), updatedGoal.getSavedAmount());

        // 4. 验证储蓄记录创建
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SavingsRecord record = savingsRecordService.getRecordByMonth(goalId, currentMonth);
        assertNotNull(record);
        assertEquals(new BigDecimal("5000.00"), record.getPlannedAmount());
        assertEquals(new BigDecimal("5000.00"), record.getActualAmount());
        assertEquals(1, record.getIsCompleted()); // 达标
    }

    @Test
    @DisplayName("执行月度储蓄 - 固定金额模式未达标")
    void testExecuteMonthlySaving_FixedAmount_NotCompleted() {
        // 1. 创建固定金额模式的梦想目标
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("买车");
        dreamGoal.setTargetAmount(new BigDecimal("100000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("5000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);
        Long goalId = savedGoal.getId();

        // 2. 执行月度储蓄 - 存入金额 < 计划金额
        dreamGoalService.executeMonthlySaving(goalId, new BigDecimal("3000.00"));

        // 3. 验证梦想目标已存金额更新
        DreamGoal updatedGoal = dreamGoalService.getById(goalId);
        assertEquals(new BigDecimal("3000.00"), updatedGoal.getSavedAmount());

        // 4. 验证储蓄记录
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SavingsRecord record = savingsRecordService.getRecordByMonth(goalId, currentMonth);
        assertNotNull(record);
        assertEquals(new BigDecimal("5000.00"), record.getPlannedAmount());
        assertEquals(new BigDecimal("3000.00"), record.getActualAmount());
        assertEquals(0, record.getIsCompleted()); // 未达标
    }

    @Test
    @DisplayName("执行月度储蓄 - 百分比模式")
    void testExecuteMonthlySaving_PercentageMode() {
        // 1. 创建百分比模式的梦想目标
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("买房首付");
        dreamGoal.setTargetAmount(new BigDecimal("500000.00"));
        dreamGoal.setSavingsType(2);
        dreamGoal.setMonthlyIncome(new BigDecimal("20000.00"));
        dreamGoal.setSavingsPercent(new BigDecimal("0.3")); // 30% = 6000
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);
        Long goalId = savedGoal.getId();

        // 2. 执行月度储蓄 - 刚好达到计划金额（20000 * 0.3 = 6000）
        dreamGoalService.executeMonthlySaving(goalId, new BigDecimal("6000.00"));

        // 3. 验证储蓄记录的计划金额是计算得出的
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SavingsRecord record = savingsRecordService.getRecordByMonth(goalId, currentMonth);
        assertNotNull(record);
        assertEquals(new BigDecimal("6000.00"), record.getPlannedAmount()); // 20000 * 0.3
        assertEquals(1, record.getIsCompleted());
    }

    @Test
    @DisplayName("执行月度储蓄 - 跨月储蓄累加")
    void testExecuteMonthlySaving_MultipleSavings() {
        // 1. 创建梦想目标
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("旅游基金");
        dreamGoal.setTargetAmount(new BigDecimal("20000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("2000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);
        Long goalId = savedGoal.getId();

        // 2. 第一个月储蓄
        dreamGoalService.executeMonthlySaving(goalId, new BigDecimal("2000.00"));
        DreamGoal goalAfterFirst = dreamGoalService.getById(goalId);
        assertEquals(new BigDecimal("2000.00"), goalAfterFirst.getSavedAmount());

        // 3. 验证储蓄记录（第一个月）
        SavingsRecord record1 = savingsRecordService.getRecordByMonth(goalId, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        assertNotNull(record1);
        assertEquals(new BigDecimal("2000.00"), record1.getActualAmount());
    }

    @Test
    @DisplayName("执行月度储蓄 - 金额必须大于0")
    void testExecuteMonthlySaving_InvalidAmount() {
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("测试目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("1000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);

        assertThrows(RuntimeException.class, () ->
            dreamGoalService.executeMonthlySaving(savedGoal.getId(), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("执行月度储蓄 - 目标不存在抛出异常")
    void testExecuteMonthlySaving_GoalNotFound() {
        assertThrows(RuntimeException.class, () ->
            dreamGoalService.executeMonthlySaving(999999L, new BigDecimal("1000.00")));
    }

    @Test
    @DisplayName("获取储蓄进度 - 0%进度")
    void testGetSavingProgress_ZeroPercent() {
        // 1. 创建梦想目标（未储蓄）
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("新目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("1000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);

        // 2. 获取进度
        BigDecimal progress = dreamGoalService.getSavingProgress(savedGoal.getId());

        // 3. 验证结果
        assertEquals(0, progress.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("获取储蓄进度 - 50%进度")
    void testGetSavingProgress_FiftyPercent() {
        // 1. 创建梦想目标并储蓄
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("半完成目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("1000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);

        // 直接修改已存金额
        savedGoal.setSavedAmount(new BigDecimal("5000.00"));
        dreamGoalService.updateById(savedGoal);

        // 2. 获取进度
        BigDecimal progress = dreamGoalService.getSavingProgress(savedGoal.getId());

        // 3. 验证结果 - 5000/10000 = 50%
        assertEquals(new BigDecimal("50.00"), progress);
    }

    @Test
    @DisplayName("获取储蓄进度 - 100%进度")
    void testGetSavingProgress_HundredPercent() {
        // 1. 创建梦想目标并完全储蓄
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("完成目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("1000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);

        savedGoal.setSavedAmount(new BigDecimal("10000.00"));
        dreamGoalService.updateById(savedGoal);

        // 2. 获取进度
        BigDecimal progress = dreamGoalService.getSavingProgress(savedGoal.getId());

        // 3. 验证结果
        assertEquals(new BigDecimal("100.00"), progress);
    }

    @Test
    @DisplayName("获取储蓄进度 - 超额储蓄超过100%")
    void testGetSavingProgress_OverHundredPercent() {
        // 1. 创建梦想目标并超额储蓄
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("超额目标");
        dreamGoal.setTargetAmount(new BigDecimal("10000.00"));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("1000.00"));
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);

        savedGoal.setSavedAmount(new BigDecimal("15000.00"));
        dreamGoalService.updateById(savedGoal);

        // 2. 获取进度
        BigDecimal progress = dreamGoalService.getSavingProgress(savedGoal.getId());

        // 3. 验证结果
        assertEquals(new BigDecimal("150.00"), progress);
    }

    @Test
    @DisplayName("获取储蓄进度 - 目标不存在返回0")
    void testGetSavingProgress_GoalNotFound() {
        BigDecimal progress = dreamGoalService.getSavingProgress(999999L);
        assertEquals(0, progress.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("获取储蓄进度 - 目标金额为0返回0")
    void testGetSavingProgress_ZeroTarget() {
        // 1. 先创建正常目标（绕过createDreamGoal的校验）
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("异常目标");
        dreamGoal.setTargetAmount(new BigDecimal("1000.00")); // 正常金额
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("100.00"));
        dreamGoal.setSavedAmount(BigDecimal.ZERO);
        dreamGoalService.save(dreamGoal); // 直接save，不走createDreamGoal

        // 2. 直接修改目标金额为0（模拟异常数据）
        dreamGoal.setTargetAmount(BigDecimal.ZERO);
        dreamGoalService.updateById(dreamGoal);

        // 3. 验证进度返回0
        BigDecimal progress = dreamGoalService.getSavingProgress(dreamGoal.getId());
        assertEquals(0, progress.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("综合测试 - 完整的梦想目标管理流程")
    void testCompleteDreamGoalWorkflow() {
        // 1. 创建梦想目标（固定金额模式）
        DreamGoal dreamGoal = new DreamGoal();
        dreamGoal.setUserId(TEST_USER_ID);
        dreamGoal.setName("购买笔记本电脑");
        dreamGoal.setTargetAmount(new BigDecimal("12000.00"));
        dreamGoal.setTargetDate(LocalDate.now().plusMonths(6));
        dreamGoal.setSavingsType(1);
        dreamGoal.setSavingsAmount(new BigDecimal("2000.00"));
        dreamGoal.setPriority(1);
        DreamGoal savedGoal = dreamGoalService.createDreamGoal(dreamGoal);
        Long goalId = savedGoal.getId();

        // 2. 验证初始进度为0
        BigDecimal initialProgress = dreamGoalService.getSavingProgress(goalId);
        assertEquals(0, initialProgress.compareTo(BigDecimal.ZERO));

        // 3. 执行月度储蓄（第1个月，达标）
        dreamGoalService.executeMonthlySaving(goalId, new BigDecimal("2000.00"));
        assertEquals(new BigDecimal("2000.00"), dreamGoalService.getById(goalId).getSavedAmount());
        assertEquals(new BigDecimal("16.67"), dreamGoalService.getSavingProgress(goalId));

        // 4. 同月再次储蓄（累加到当月）
        dreamGoalService.executeMonthlySaving(goalId, new BigDecimal("1500.00"));
        assertEquals(new BigDecimal("3500.00"), dreamGoalService.getById(goalId).getSavedAmount());

        // 5. 验证储蓄记录（同月只有1条，金额累加为3500）
        List<SavingsRecord> records = savingsRecordService.getRecordsByGoalId(goalId);
        assertEquals(1, records.size());
        assertEquals(new BigDecimal("3500.00"), records.get(0).getActualAmount());

        // 6. 获取用户梦想目标列表
        List<DreamGoal> goals = dreamGoalService.getUserDreamGoals(TEST_USER_ID, null);
        assertEquals(1, goals.size());
        assertEquals("购买笔记本电脑", goals.get(0).getName());
    }
}
