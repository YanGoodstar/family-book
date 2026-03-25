package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.BalanceRequest;
import com.familybook.dto.request.LoginRequest;
import com.familybook.dto.request.UserUpdateRequest;
import com.familybook.entity.User;
import com.familybook.service.DreamGoalService;
import com.familybook.service.UserService;
import com.familybook.vo.BalanceVO;
import com.familybook.vo.LoginVO;
import com.familybook.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "用户管理", description = "用户登录、用户信息相关接口")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DreamGoalService dreamGoalService;

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

    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息，未登录返回null")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        User user = userService.getCurrentUser();

        // 未登录用户返回null，不报错
        if (user == null) {
            return Result.success(null);
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

    @Operation(summary = "获取用户余额", description = "获取当前用户的起始金额和当前余额，未登录返回0")
    @GetMapping("/balance")
    public Result<BalanceVO> getBalance() {
        User user = userService.getCurrentUser();

        BalanceVO vo = new BalanceVO();

        if (user == null) {
            vo.setInitialBalance(BigDecimal.ZERO);
            vo.setCurrentBalance(BigDecimal.ZERO);
            vo.setCommittedSavings(BigDecimal.ZERO);
            vo.setSpendableBalance(BigDecimal.ZERO);
            vo.setOverCommitted(false);
            return Result.success(vo);
        }

        BigDecimal currentBalance = userService.calculateBalance(user.getId());
        BigDecimal committedSavings = dreamGoalService.getCommittedSavings(user.getId());

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        if (committedSavings == null) {
            committedSavings = BigDecimal.ZERO;
        }

        BigDecimal spendableBalance = currentBalance.subtract(committedSavings);

        vo.setInitialBalance(user.getInitialBalance() != null ? user.getInitialBalance() : BigDecimal.ZERO);
        vo.setCurrentBalance(currentBalance);
        vo.setCommittedSavings(committedSavings);
        vo.setSpendableBalance(spendableBalance);
        vo.setOverCommitted(spendableBalance.compareTo(BigDecimal.ZERO) < 0);

        return Result.success(vo);
    }

    @Operation(summary = "设置起始金额", description = "设置用户的起始金额，会重新计算当前余额")
    @PostMapping("/balance")
    public Result<BalanceVO> setBalance(@RequestBody BalanceRequest request) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        userService.setInitialBalance(user.getId(), request.getInitialBalance());

        // 重新计算余额
        BigDecimal currentBalance = userService.calculateBalance(user.getId());
        BigDecimal committedSavings = dreamGoalService.getCommittedSavings(user.getId());

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        if (committedSavings == null) {
            committedSavings = BigDecimal.ZERO;
        }

        BigDecimal spendableBalance = currentBalance.subtract(committedSavings);

        BalanceVO vo = new BalanceVO();
        vo.setInitialBalance(request.getInitialBalance());
        vo.setCurrentBalance(currentBalance);
        vo.setCommittedSavings(committedSavings);
        vo.setSpendableBalance(spendableBalance);
        vo.setOverCommitted(spendableBalance.compareTo(BigDecimal.ZERO) < 0);

        return Result.success(vo);
    }
}
