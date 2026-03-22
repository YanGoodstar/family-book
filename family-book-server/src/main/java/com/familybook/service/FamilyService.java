package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Family;

import java.util.List;

/**
 * 家庭组服务接口
 */
public interface FamilyService extends IService<Family> {

    /**
     * 创建家庭组
     */
    Family createFamily(String name);

    /**
     * 通过邀请码加入家庭
     */
    void joinFamilyByCode(String code);

    /**
     * 获取用户家庭组列表
     */
    List<Family> getUserFamilies(Long userId);

    /**
     * 生成邀请码
     */
    String generateInviteCode(Long familyId);
}
