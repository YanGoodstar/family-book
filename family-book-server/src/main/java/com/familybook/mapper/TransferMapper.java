package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Transfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 转账记录Mapper接口
 * 用于操作t_transfer表，记录用户账户之间的转账操作
 */
@Mapper
public interface TransferMapper extends BaseMapper<Transfer> {
}
