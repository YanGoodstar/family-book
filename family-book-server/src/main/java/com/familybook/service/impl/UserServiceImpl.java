package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.User;
import com.familybook.mapper.UserMapper;
import com.familybook.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getByOpenid(String openid) {
        return null;
    }

    @Override
    public String wxLogin(String code) {
        return null;
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public void updateUserInfo(User user) {
        // TODO: 待实现
    }
}
