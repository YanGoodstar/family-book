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
import java.util.List;

/**
 * 梦想目标服务实现类
 */
@Service
public class DreamGoalServiceImpl extends ServiceImpl<DreamGoalMapper, DreamGoal> implements DreamGoalService {

    private final DreamGoalMapper dreamGoalMapper;
    private final SavingsRecordService savingsRecordService;

    public DreamGoalServiceImpl(DreamGoalMapper dreamGoalMapper, SavingsRecordService savingsRecordService) {
        this.dreamGoalMapper = dreamGoalMapper;
        this.savingsRecordService = savingsRecordService;
    }

    @Override
    public DreamGoal createDreamGoal(DreamGoal dreamGoal) {
        // 参数校验
        if (dreamGoal.getTargetAmount() == null || dreamGoal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("目标金额必须大于0");
        }

        // 设置默认值
        if (dreamGoal.getSavedAmount() == null) {
            dreamGoal.setSavedAmount(BigDecimal.ZERO);
        }

        // 校验储蓄模式参数
        if (dreamGoal.getSavingsType() == null) {
            dreamGoal.setSavingsType(1); // 默认固定金额
        }

        if (dreamGoal.getSavingsType() == 1) {
            // 固定金额模式
            if (dreamGoal.getSavingsAmount() == null || dreamGoal.getSavingsAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("固定储蓄金额必须大于0");
            }
        } else if (dreamGoal.getSavingsType() == 2) {
            // 工资百分比模式
            if (dreamGoal.getMonthlyIncome() == null || dreamGoal.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("月收入必须大于0");
            }
            if (dreamGoal.getSavingsPercent() == null || dreamGoal.getSavingsPercent().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("储蓄百分比必须大于0");
            }
        }

        // 设置默认优先级
        if (dreamGoal.getPriority() == null) {
            dreamGoal.setPriority(1);
        }

        this.save(dreamGoal);
        return dreamGoal;
    }

    @Override
    public List<DreamGoal> getUserDreamGoals(Long userId, Long familyId) {
        LambdaQueryWrapper<DreamGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamGoal::getUserId, userId)
                .orderByAsc(DreamGoal::getPriority)
                .orderByDesc(DreamGoal::getCreateTime);

        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeMonthlySaving(Long dreamGoalId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("储蓄金额必须大于0");
        }

        DreamGoal dreamGoal = this.getById(dreamGoalId);
        if (dreamGoal == null) {
            throw new RuntimeException("梦想目标不存在");
        }

        // 更新已存金额
        BigDecimal currentSaved = dreamGoal.getSavedAmount() != null ? dreamGoal.getSavedAmount() : BigDecimal.ZERO;
        BigDecimal newSaved = currentSaved.add(amount);
        dreamGoal.setSavedAmount(newSaved);
        this.updateById(dreamGoal);

        // 创建或更新储蓄记录
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SavingsRecord existingRecord = savingsRecordService.getRecordByMonth(dreamGoalId, currentMonth);

        if (existingRecord != null) {
            // 已有记录，累加金额
            BigDecimal newActualAmount = existingRecord.getActualAmount().add(amount);
            existingRecord.setActualAmount(newActualAmount);
            // 重新判断是否达标
            boolean isCompleted = newActualAmount.compareTo(existingRecord.getPlannedAmount()) >= 0;
            existingRecord.setIsCompleted(isCompleted ? 1 : 0);
            savingsRecordService.updateById(existingRecord);
        } else {
            // 创建新记录
            SavingsRecord record = new SavingsRecord();
            record.setGoalId(dreamGoalId);
            record.setUserId(dreamGoal.getUserId());
            record.setRecordMonth(currentMonth);
            record.setPlannedAmount(calculatePlannedAmount(dreamGoal));
            record.setActualAmount(amount);
            // 判断是否达标
            boolean isCompleted = amount.compareTo(record.getPlannedAmount()) >= 0;
            record.setIsCompleted(isCompleted ? 1 : 0);
            savingsRecordService.save(record);
        }
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
}
