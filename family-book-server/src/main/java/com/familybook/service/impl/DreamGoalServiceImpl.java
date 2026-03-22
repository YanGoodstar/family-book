package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.DreamGoal;
import com.familybook.mapper.DreamGoalMapper;
import com.familybook.service.DreamGoalService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 梦想目标服务实现类
 */
@Service
public class DreamGoalServiceImpl extends ServiceImpl<DreamGoalMapper, DreamGoal> implements DreamGoalService {

    private final DreamGoalMapper dreamGoalMapper;

    public DreamGoalServiceImpl(DreamGoalMapper dreamGoalMapper) {
        this.dreamGoalMapper = dreamGoalMapper;
    }

    @Override
    public DreamGoal createDreamGoal(DreamGoal dreamGoal) {
        return null;
    }

    @Override
    public List<DreamGoal> getUserDreamGoals(Long userId, Long familyId) {
        return null;
    }

    @Override
    public void executeMonthlySaving(Long dreamGoalId, BigDecimal amount) {
    }

    @Override
    public BigDecimal getSavingProgress(Long dreamGoalId) {
        return null;
    }
}
