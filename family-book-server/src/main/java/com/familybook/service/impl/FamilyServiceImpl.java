package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Family;
import com.familybook.mapper.FamilyMapper;
import com.familybook.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 家庭组服务实现类
 */
@Service
public class FamilyServiceImpl extends ServiceImpl<FamilyMapper, Family> implements FamilyService {

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public Family createFamily(String name) {
        return null;
    }

    @Override
    public void joinFamilyByCode(String code) {

    }

    @Override
    public List<Family> getUserFamilies(Long userId) {
        return null;
    }

    @Override
    public String generateInviteCode(Long familyId) {
        return null;
    }
}
