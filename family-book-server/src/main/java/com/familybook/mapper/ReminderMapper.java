package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记账提醒Mapper接口
 * 用于操作t_reminder表，提供记账提醒相关的数据访问方法
 */
@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {

}
