package com.familybook.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.User;
import com.familybook.mapper.UserMapper;
import com.familybook.security.JwtTokenProvider;
import com.familybook.security.SecurityUtils;
import com.familybook.service.UserService;
import com.familybook.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final HttpUtils httpUtils;

    @Value("${wechat.appid}")
    private String wxAppId;

    @Value("${wechat.secret}")
    private String wxSecret;

    @Override
    public User getByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public String wxLogin(String code) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            wxAppId, wxSecret, code
        );

        JSONObject response = httpUtils.get(url);
        if (response == null || response.containsKey("errcode")) {
            log.error("WeChat login failed: {}", response);
            throw new RuntimeException("微信登录失败");
        }

        String openid = response.getString("openid");

        User user = getByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户" + UUID.randomUUID().toString().substring(0, 6));
            userMapper.insert(user);
        }

        return jwtTokenProvider.generateToken(user.getId());
    }

    @Override
    public User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }

    @Override
    public void updateUserInfo(User user) {
        userMapper.updateById(user);
    }
}
