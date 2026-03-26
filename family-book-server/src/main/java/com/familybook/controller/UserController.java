package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.BalanceRequest;
import com.familybook.dto.request.LoginRequest;
import com.familybook.dto.request.UserUpdateRequest;
import com.familybook.entity.User;
import com.familybook.security.JwtTokenProvider;
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

@Tag(name = "用户管理", description = "用户登录、用户信息与资产接口")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DreamGoalService dreamGoalService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "微信登录", description = "微信小程序登录，传入 code 换取 token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        String token = userService.wxLogin(request.getCode());
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.getById(userId);

        UserVO userVO = new UserVO();
        if (user != null) {
            BeanUtils.copyProperties(user, userVO);
        }

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);
        return Result.success(loginVO);
    }

    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息，未登录返回 null")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        User user = userService.getCurrentUser();
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

    @Operation(summary = "获取用户余额", description = "返回余额、承诺储蓄与登录/初始化状态")
    @GetMapping("/balance")
    public Result<BalanceVO> getBalance() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.success(buildAnonymousBalance());
        }

        BigDecimal currentBalance = userService.calculateBalance(user.getId());
        BigDecimal committedSavings = dreamGoalService.getCommittedSavings(user.getId());
        return Result.success(buildLoggedInBalance(user, currentBalance, committedSavings));
    }

    @Operation(summary = "设置起始金额", description = "设置用户起始金额，并返回更新后的余额视图")
    @PostMapping("/balance")
    public Result<BalanceVO> setBalance(@RequestBody BalanceRequest request) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        userService.setInitialBalance(user.getId(), request.getInitialBalance());

        BigDecimal currentBalance = userService.calculateBalance(user.getId());
        BigDecimal committedSavings = dreamGoalService.getCommittedSavings(user.getId());
        user.setInitialBalance(request.getInitialBalance());
        return Result.success(buildLoggedInBalance(user, currentBalance, committedSavings));
    }

    private BalanceVO buildAnonymousBalance() {
        BalanceVO vo = new BalanceVO();
        vo.setLoggedIn(false);
        vo.setInitialBalanceSet(false);
        vo.setInitialBalance(BigDecimal.ZERO);
        vo.setCurrentBalance(BigDecimal.ZERO);
        vo.setCommittedSavings(BigDecimal.ZERO);
        vo.setSpendableBalance(BigDecimal.ZERO);
        vo.setOverCommitted(false);
        return vo;
    }

    private BalanceVO buildLoggedInBalance(User user, BigDecimal currentBalance, BigDecimal committedSavings) {
        BigDecimal safeCurrentBalance = currentBalance != null ? currentBalance : BigDecimal.ZERO;
        BigDecimal safeCommittedSavings = committedSavings != null ? committedSavings : BigDecimal.ZERO;
        BigDecimal safeInitialBalance = user.getInitialBalance() != null ? user.getInitialBalance() : BigDecimal.ZERO;
        BigDecimal spendableBalance = safeCurrentBalance.subtract(safeCommittedSavings);

        BalanceVO vo = new BalanceVO();
        vo.setLoggedIn(true);
        vo.setInitialBalanceSet(user.getInitialBalance() != null);
        vo.setInitialBalance(safeInitialBalance);
        vo.setCurrentBalance(safeCurrentBalance);
        vo.setCommittedSavings(safeCommittedSavings);
        vo.setSpendableBalance(spendableBalance);
        vo.setOverCommitted(spendableBalance.compareTo(BigDecimal.ZERO) < 0);
        return vo;
    }
}
