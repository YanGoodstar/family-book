package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账户Mapper接口
 * 用于操作账户表(t_account)的数据访问层
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
