package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.SavingsRecord;

import java.util.List;

/**
 * 储蓄记录服务接口
 */
public interface SavingsRecordService extends IService<SavingsRecord> {

    /**
     * 获取储蓄记录列表
     */
    List<SavingsRecord> getRecordsByGoalId(Long goalId);

    /**
     * 获取最近的储蓄记录
     */
    List<SavingsRecord> getRecentRecordsByGoalId(Long goalId, int limit);
}
