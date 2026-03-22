package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.SavingsRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 储蓄记录Mapper接口
 * 用于操作t_savings_record表，记录每月自动储蓄的金额
 */
@Mapper
public interface SavingsRecordMapper extends BaseMapper<SavingsRecord> {
}
