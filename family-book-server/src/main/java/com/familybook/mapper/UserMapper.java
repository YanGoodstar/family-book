package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 用于操作用户表(t_user)的数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
