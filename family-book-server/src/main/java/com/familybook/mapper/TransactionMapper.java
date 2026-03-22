package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易记录Mapper接口
 * 用于操作t_transaction表，提供交易记录的CRUD操作
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}
