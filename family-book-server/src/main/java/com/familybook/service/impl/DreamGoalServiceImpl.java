package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.DreamGoal;
import com.familybook.entity.SavingsRecord;
import com.familybook.mapper.DreamGoalMapper;
import com.familybook.service.DreamGoalService;
import com.familybook.service.SavingsRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 梦想目标服务实现类
 */
@Service
public class DreamGoalServiceImpl extends ServiceImpl<DreamGoalMapper, DreamGoal> implements DreamGoalService {

    private final SavingsRecordService savingsRecordService;
    private final DreamGoalMapper dreamGoalMapper;

    public DreamGoalServiceImpl(SavingsRecordService savingsRecordService, DreamGoalMapper dreamGoalMapper) {
        this.savingsRecordService = savingsRecordService;
        this.dreamGoalMapper = dreamGoalMapper;
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

        this.save(dreamGoal);
        return dreamGoal;
    }

    @Override
    public List<DreamGoal> getUserDreamGoals(Long userId) {
        LambdaQueryWrapper<DreamGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamGoal::getUserId, userId)
                .orderByAsc(DreamGoal::getPriority)
                .orderByDesc(DreamGoal::getUpdateTime)
                .orderByDesc(DreamGoal::getCreateTime);

        List<DreamGoal> goals = this.list(wrapper);
        goals.sort(Comparator.comparing(this::isGoalCompleted));
        return goals;
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
}
