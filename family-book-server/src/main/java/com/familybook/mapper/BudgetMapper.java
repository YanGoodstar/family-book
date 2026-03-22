package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Budget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算Mapper接口
 * 用于操作预算表(t_budget)的数据访问层
 * 提供预算的增删改查及自定义查询方法
 */
@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {

}
