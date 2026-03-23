package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.AccountRequest;
import com.familybook.entity.Account;
import com.familybook.security.SecurityUtils;
import com.familybook.service.AccountService;
import com.familybook.vo.AccountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "账户管理", description = "账户的增删改查、资产统计")
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "获取账户列表", description = "获取当前用户的所有账户列表")
    @GetMapping("/list")
    public Result<List<AccountVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Account> accounts = accountService.getUserAccounts(userId);

        List<AccountVO> voList = accounts.stream()
                .map(a -> {
                    AccountVO vo = new AccountVO();
                    BeanUtils.copyProperties(a, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "创建账户", description = "创建新的资产账户（现金、银行卡、支付宝等）")
    @PostMapping
    public Result<AccountVO> create(@RequestBody AccountRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Account account = new Account();
        BeanUtils.copyProperties(request, account);
        account.setUserId(userId);

        Account saved = accountService.createAccount(account);

        AccountVO vo = new AccountVO();
        BeanUtils.copyProperties(saved, vo);
        return Result.success(vo);
    }

    @Operation(summary = "更新账户", description = "更新账户信息")
    @PutMapping("/{id}")
    public Result<AccountVO> update(@PathVariable Long id, @RequestBody AccountRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Account account = accountService.getById(id);
        if (account == null) {
            return Result.error("账户不存在");
        }

        if (!account.getUserId().equals(userId)) {
            return Result.error("无权操作此账户");
        }

        BeanUtils.copyProperties(request, account);
        account.setId(id);

        accountService.updateById(account);

        AccountVO vo = new AccountVO();
        BeanUtils.copyProperties(account, vo);
        return Result.success(vo);
    }

    @Operation(summary = "删除账户", description = "删除账户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Account account = accountService.getById(id);
        if (account == null) {
            return Result.error("账户不存在");
        }

        if (!account.getUserId().equals(userId)) {
            return Result.error("无权操作此账户");
        }

        accountService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "获取账户详情", description = "根据ID获取账户详情")
    @GetMapping("/{id}")
    public Result<AccountVO> getById(@PathVariable Long id) {
        Account account = accountService.getById(id);
        if (account == null) {
            return Result.error("账户不存在");
        }

        AccountVO vo = new AccountVO();
        BeanUtils.copyProperties(account, vo);
        return Result.success(vo);
    }

    @Operation(summary = "获取资产统计", description = "获取用户总资产和账户余额汇总")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Account> accounts = accountService.getUserAccounts(userId);
        BigDecimal totalAssets = accountService.getTotalAssets(userId);

        List<AccountVO> voList = accounts.stream()
                .map(a -> {
                    AccountVO vo = new AccountVO();
                    BeanUtils.copyProperties(a, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalAssets", totalAssets);
        result.put("accounts", voList);
        result.put("accountCount", accounts.size());

        return Result.success(result);
    }
}
