package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.Family;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家庭组Mapper接口
 * 用于操作t_family表的数据访问层
 */
@Mapper
public interface FamilyMapper extends BaseMapper<Family> {

}
