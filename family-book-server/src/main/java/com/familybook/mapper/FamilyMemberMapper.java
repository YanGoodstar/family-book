package com.familybook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familybook.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家庭成员Mapper接口
 * 用于操作家庭成员表(t_family_member)的数据访问层
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {

}
