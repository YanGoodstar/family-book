package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 交易记录Mapper接口
 * 用于操作t_transaction表，提供交易记录的CRUD操作
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    /**
     * 根据用户ID和交易类型汇总金额
     * @param userId 用户ID
     * @param type 交易类型：1=支出，2=收入
     * @return 汇总金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_transaction WHERE user_id = #{userId} AND type = #{type} AND status = 1")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
}
