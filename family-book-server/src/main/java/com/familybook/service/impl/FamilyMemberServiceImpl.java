package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.FamilyMember;
import com.familybook.mapper.FamilyMemberMapper;
import com.familybook.service.FamilyMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 家庭成员服务实现类
 */
@Service
public class FamilyMemberServiceImpl extends ServiceImpl<FamilyMemberMapper, FamilyMember> implements FamilyMemberService {

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Override
    public List<FamilyMember> getMembersByFamilyId(Long familyId) {
        return null;
    }

    @Override
    public void addMember(Long familyId, Long userId, Integer role) {

    }

    @Override
    public void removeMember(Long familyId, Long userId) {

    }

    @Override
    public void setDefaultFamily(Long userId, Long familyId) {

    }
}
