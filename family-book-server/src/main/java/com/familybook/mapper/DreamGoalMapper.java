package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.DreamGoal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 梦想目标Mapper接口
 * 用于操作梦想目标表(t_dream_goal)的数据访问层
 */
@Mapper
public interface DreamGoalMapper extends BaseMapper<DreamGoal> {
}
