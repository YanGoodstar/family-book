package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.DreamGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 梦想目标Mapper接口
 * 用于操作梦想目标表(t_dream_goal)的数据访问层
 */
@Mapper
public interface DreamGoalMapper extends BaseMapper<DreamGoal> {

    @Select("""
            SELECT COALESCE(SUM(saved_amount), 0)
            FROM t_dream_goal
            WHERE user_id = #{userId} AND status = 1
            """)
    BigDecimal sumCommittedSavingsByUserId(Long userId);
}
