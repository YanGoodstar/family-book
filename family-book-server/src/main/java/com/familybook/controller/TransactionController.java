package com.familybook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.familybook.common.Result;
import com.familybook.dto.request.TransactionQueryRequest;
import com.familybook.dto.request.TransactionRequest;
import com.familybook.entity.Transaction;
import com.familybook.security.SecurityUtils;
import com.familybook.service.TransactionService;
import com.familybook.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "记账管理", description = "记账、查询、统计相关接口")
@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "新增记账", description = "记录一笔收入或支出")
    @PostMapping
    public Result<TransactionVO> record(@RequestBody TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(request, transaction);
        transaction.setUserId(userId);

        Transaction saved = transactionService.record(transaction);

        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(saved, vo);
        return Result.success(vo);
    }

    @Operation(summary = "更新记账", description = "更新记账记录")
    @PutMapping("/{id}")
    public Result<TransactionVO> update(@PathVariable Long id, @RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(request, transaction);
        transaction.setId(id);
        transactionService.updateById(transaction);

        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);
        return Result.success(vo);
    }

    @Operation(summary = "删除记账", description = "删除记账记录，自动回滚账户余额")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return Result.success();
    }

    @Operation(summary = "获取记账详情", description = "根据ID获取记账详情")
    @GetMapping("/{id}")
    public Result<TransactionVO> getById(@PathVariable Long id) {
        Transaction transaction = transactionService.getById(id);
        if (transaction == null) {
            return Result.error("记录不存在");
        }

        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);
        return Result.success(vo);
    }

    @Operation(summary = "查询记账列表", description = "分页查询记账记录，支持时间、类型、分类筛选")
    @GetMapping("/list")
    public Result<PageVO<TransactionVO>> list(TransactionQueryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Transaction> list = transactionService.getTransactions(
                userId,
                request.getFamilyId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getType(),
                request.getCategoryId()
        );

        List<TransactionVO> voList = list.stream()
                .map(t -> {
                    TransactionVO vo = new TransactionVO();
                    BeanUtils.copyProperties(t, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        PageVO<TransactionVO> pageVO = PageVO.of(
                request.getPageNum() != null ? request.getPageNum() : 1,
                request.getPageSize() != null ? request.getPageSize() : 20,
                (long) voList.size(),
                (voList.size() + 19) / 20,
                voList
        );

        return Result.success(pageVO);
    }

    @Operation(summary = "收支统计", description = "统计指定时间段内的收支总额")
    @GetMapping("/statistics")
    public Result<TransactionStatisticsVO> statistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtils.getCurrentUserId();

        // type: 1=收入, 2=支出
        BigDecimal income = transactionService.getStatistics(userId, 1,
                startDate != null ? java.time.LocalDate.parse(startDate) : null,
                endDate != null ? java.time.LocalDate.parse(endDate) : null);
        BigDecimal expense = transactionService.getStatistics(userId, 2,
                startDate != null ? java.time.LocalDate.parse(startDate) : null,
                endDate != null ? java.time.LocalDate.parse(endDate) : null);

        TransactionStatisticsVO vo = new TransactionStatisticsVO();
        vo.setIncome(income);
        vo.setExpense(expense);
        vo.setBalance(income.subtract(expense));

        return Result.success(vo);
    }
}
