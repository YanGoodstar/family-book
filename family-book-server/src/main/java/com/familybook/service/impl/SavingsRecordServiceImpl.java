package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.SavingsRecord;
import com.familybook.mapper.SavingsRecordMapper;
import com.familybook.service.SavingsRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 储蓄记录服务实现类
 */
@Service
public class SavingsRecordServiceImpl extends ServiceImpl<SavingsRecordMapper, SavingsRecord> implements SavingsRecordService {

    private final SavingsRecordMapper savingsRecordMapper;

    public SavingsRecordServiceImpl(SavingsRecordMapper savingsRecordMapper) {
        this.savingsRecordMapper = savingsRecordMapper;
    }

    @Override
    public List<SavingsRecord> getRecordsByDreamGoalId(Long dreamGoalId) {
        return null;
    }

    @Override
    public SavingsRecord getRecordByMonth(Long dreamGoalId, Integer year, Integer month) {
        return null;
    }
}
