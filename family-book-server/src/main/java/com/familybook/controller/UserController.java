package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.LoginRequest;
import com.familybook.dto.request.UserUpdateRequest;
import com.familybook.entity.User;
import com.familybook.service.UserService;
import com.familybook.vo.LoginVO;
import com.familybook.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户登录、用户信息相关接口")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "微信登录", description = "微信小程序登录，传入code换取token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        String token = userService.wxLogin(request.getCode());

        User user = userService.getCurrentUser();
        UserVO userVO = new UserVO();
        if (user != null) {
            BeanUtils.copyProperties(user, userVO);
        }

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);

        return Result.success(loginVO);
    }

    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息", description = "更新用户昵称、头像等信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UserUpdateRequest request) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        BeanUtils.copyProperties(request, user);
        userService.updateUserInfo(user);
        return Result.success();
    }
}
