package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.FamilyMember;

import java.util.List;

/**
 * 家庭成员服务接口
 */
public interface FamilyMemberService extends IService<FamilyMember> {

    /**
     * 获取家庭成员列表
     */
    List<FamilyMember> getMembersByFamilyId(Long familyId);

    /**
     * 添加家庭成员
     */
    void addMember(Long familyId, Long userId, Integer role);

    /**
     * 移除家庭成员
     */
    void removeMember(Long familyId, Long userId);

    /**
     * 设置默认家庭
     */
    void setDefaultFamily(Long userId, Long familyId);
}
