package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.DreamGoal;

import java.math.BigDecimal;
import java.util.List;

/**
 * 梦想目标服务接口
 */
public interface DreamGoalService extends IService<DreamGoal> {

    /**
     * 创建梦想目标
     */
    DreamGoal createDreamGoal(DreamGoal dreamGoal);

    /**
     * 获取用户梦想目标列表
     */
    List<DreamGoal> getUserDreamGoals(Long userId, Long familyId);

    /**
     * 执行月度储蓄
     */
    void executeMonthlySaving(Long dreamGoalId, BigDecimal amount);

    /**
     * 获取储蓄进度
     */
    BigDecimal getSavingProgress(Long dreamGoalId);
}
