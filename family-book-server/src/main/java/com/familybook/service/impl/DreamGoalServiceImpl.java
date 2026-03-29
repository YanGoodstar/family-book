package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.constant.DreamGoalStatus;
import com.familybook.entity.Category;
import com.familybook.entity.DreamGoal;
import com.familybook.entity.SavingsRecord;
import com.familybook.entity.Transaction;
import com.familybook.mapper.CategoryMapper;
import com.familybook.mapper.DreamGoalMapper;
import com.familybook.service.DreamGoalService;
import com.familybook.service.SavingsRecordService;
import com.familybook.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 梦想目标服务实现类
 */
@Service
public class DreamGoalServiceImpl extends ServiceImpl<DreamGoalMapper, DreamGoal> implements DreamGoalService {

    private final SavingsRecordService savingsRecordService;
    private final DreamGoalMapper dreamGoalMapper;
    private final TransactionService transactionService;
    private final CategoryMapper categoryMapper;

    public DreamGoalServiceImpl(
            SavingsRecordService savingsRecordService,
            DreamGoalMapper dreamGoalMapper,
            TransactionService transactionService,
            CategoryMapper categoryMapper
    ) {
        this.savingsRecordService = savingsRecordService;
        this.dreamGoalMapper = dreamGoalMapper;
        this.transactionService = transactionService;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public DreamGoal createDreamGoal(DreamGoal dreamGoal) {
        validateGoal(dreamGoal);

        if (dreamGoal.getUserId() == null) {
            throw new RuntimeException("用户信息缺失");
        }

        if (dreamGoal.getSavedAmount() == null) {
            dreamGoal.setSavedAmount(BigDecimal.ZERO);
        }

        if (dreamGoal.getSavingsType() == null) {
            dreamGoal.setSavingsType(1);
        }

        if (dreamGoal.getPriority() == null) {
            dreamGoal.setPriority(0);
        }

        if (dreamGoal.getGoalStatus() == null) {
            dreamGoal.setGoalStatus(DreamGoalStatus.ACTIVE);
        }

        this.save(dreamGoal);
        return dreamGoal;
    }

    @Override
    public List<DreamGoal> getUserDreamGoals(Long userId) {
        LambdaQueryWrapper<DreamGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamGoal::getUserId, userId)
                .orderByAsc(DreamGoal::getGoalStatus)
                .orderByAsc(DreamGoal::getPriority)
                .orderByDesc(DreamGoal::getUpdateTime)
                .orderByDesc(DreamGoal::getCreateTime);

        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DreamGoal saveAmount(Long dreamGoalId, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("储蓄金额必须大于0");
        }

        DreamGoal dreamGoal = this.getById(dreamGoalId);
        if (dreamGoal == null) {
            throw new RuntimeException("梦想目标不存在");
        }

        if (DreamGoalStatus.isArchived(dreamGoal.getGoalStatus())) {
            throw new RuntimeException("目标已归档，无法继续存钱");
        }

        if (isGoalCompleted(dreamGoal)) {
            throw new RuntimeException("目标已完成，无法继续存钱");
        }

        BigDecimal currentSaved = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;
        BigDecimal newSaved = currentSaved.add(amount);
        dreamGoal.setSavedAmount(newSaved);
        this.updateById(dreamGoal);
        boolean completedAfterSave = isCompleted(newSaved, dreamGoal.getTargetAmount());

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SavingsRecord record = new SavingsRecord();
        record.setGoalId(dreamGoalId);
        record.setUserId(dreamGoal.getUserId());
        record.setRecordMonth(currentMonth);
        record.setPlannedAmount(calculatePlannedAmount(dreamGoal));
        record.setActualAmount(amount);
        record.setRemark(remark);
        record.setIsCompleted(completedAfterSave ? 1 : 0);
        savingsRecordService.save(record);

        return dreamGoal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DreamGoal archiveGoal(Long dreamGoalId, boolean createExpense, Long expenseCategoryId) {
        DreamGoal dreamGoal = this.getById(dreamGoalId);
        if (dreamGoal == null) {
            throw new RuntimeException("梦想目标不存在");
        }

        if (DreamGoalStatus.isArchived(dreamGoal.getGoalStatus())) {
            throw new RuntimeException("目标已归档");
        }

        if (createExpense) {
            createArchiveExpense(dreamGoal, expenseCategoryId);
        }

        dreamGoal.setGoalStatus(isGoalCompleted(dreamGoal)
                ? DreamGoalStatus.ARCHIVED_COMPLETED
                : DreamGoalStatus.ARCHIVED_STOPPED);
        this.updateById(dreamGoal);
        return dreamGoal;
    }

    @Override
    public BigDecimal getSavingProgress(Long dreamGoalId) {
        DreamGoal dreamGoal = this.getById(dreamGoalId);
        if (dreamGoal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal targetAmount = dreamGoal.getTargetAmount();
        BigDecimal savedAmount = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;

        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return savedAmount.multiply(new BigDecimal("100"))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算计划储蓄金额
     */
    @Override
    public BigDecimal getCommittedSavings(Long userId) {
        BigDecimal amount = dreamGoalMapper.sumCommittedSavingsByUserId(userId);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private void createArchiveExpense(DreamGoal dreamGoal, Long expenseCategoryId) {
        BigDecimal savedAmount = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;
        if (savedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("当前已存金额为0，无法生成支出");
        }

        if (expenseCategoryId == null) {
            throw new RuntimeException("请选择支出分类");
        }

        Category category = categoryMapper.selectById(expenseCategoryId);
        if (!isValidExpenseCategory(dreamGoal.getUserId(), category)) {
            throw new RuntimeException("请选择有效的支出分类");
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(dreamGoal.getUserId());
        transaction.setCategoryId(expenseCategoryId);
        transaction.setType(1);
        transaction.setAmount(savedAmount);
        transaction.setRemark(buildArchiveExpenseRemark(dreamGoal.getName()));
        transaction.setTransactionDate(LocalDate.now());
        transaction.setTransactionTime(LocalTime.now().withNano(0));
        transactionService.record(transaction);
    }

    private BigDecimal calculatePlannedAmount(DreamGoal dreamGoal) {
        if (dreamGoal.getSavingsType() == 1) {
            // 固定金额
            return dreamGoal.getSavingsAmount() != null ? dreamGoal.getSavingsAmount() : BigDecimal.ZERO;
        } else if (dreamGoal.getSavingsType() == 2) {
            // 工资百分比
            BigDecimal income = dreamGoal.getMonthlyIncome() != null ? dreamGoal.getMonthlyIncome() : BigDecimal.ZERO;
            BigDecimal percent = dreamGoal.getSavingsPercent() != null ? dreamGoal.getSavingsPercent() : BigDecimal.ZERO;
            return income.multiply(percent);
        }
        return BigDecimal.ZERO;
    }

    private void validateGoal(DreamGoal dreamGoal) {
        if (dreamGoal.getName() == null || dreamGoal.getName().trim().isEmpty()) {
            throw new RuntimeException("目标名称不能为空");
        }
        if (dreamGoal.getTargetAmount() == null || dreamGoal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("目标金额必须大于0");
        }
        if (dreamGoal.getSavingsType() != null && dreamGoal.getSavingsType() == 2) {
            if (dreamGoal.getMonthlyIncome() == null || dreamGoal.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("月收入必须大于0");
            }
            if (dreamGoal.getSavingsPercent() == null || dreamGoal.getSavingsPercent().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("储蓄百分比必须大于0");
            }
        }
    }

    private boolean isCompleted(BigDecimal savedAmount, BigDecimal targetAmount) {
        BigDecimal safeSavedAmount = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        BigDecimal safeTargetAmount = targetAmount != null ? targetAmount : BigDecimal.ZERO;
        return safeTargetAmount.compareTo(BigDecimal.ZERO) > 0 && safeSavedAmount.compareTo(safeTargetAmount) >= 0;
    }

    private boolean isGoalCompleted(DreamGoal dreamGoal) {
        return isCompleted(dreamGoal.getSavedAmount(), dreamGoal.getTargetAmount());
    }

    private boolean isValidExpenseCategory(Long userId, Category category) {
        if (category == null) {
            return false;
        }

        if (!Integer.valueOf(1).equals(category.getType())) {
            return false;
        }

        return category.getUserId() == null || category.getUserId().equals(userId);
    }

    private String buildArchiveExpenseRemark(String goalName) {
        String normalizedGoalName = goalName == null ? "" : goalName.trim();
        return normalizedGoalName.isEmpty()
                ? "梦想目标归档支出"
                : "梦想目标归档支出：" + normalizedGoalName;
    }
}
