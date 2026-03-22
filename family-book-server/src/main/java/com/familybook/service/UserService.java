package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据openid获取用户
     */
    User getByOpenid(String openid);

    /**
     * 微信登录
     */
    String wxLogin(String code);

    /**
     * 获取当前登录用户
     */
    User getCurrentUser();

    /**
     * 更新用户信息
     */
    void updateUserInfo(User user);
}
