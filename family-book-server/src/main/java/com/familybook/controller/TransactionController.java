package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.TransactionQueryRequest;
import com.familybook.dto.request.TransactionRequest;
import com.familybook.entity.Category;
import com.familybook.entity.Transaction;
import com.familybook.mapper.CategoryMapper;
import com.familybook.security.SecurityUtils;
import com.familybook.service.TransactionService;
import com.familybook.service.UserService;
import com.familybook.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "记账管理", description = "记账、查询、统计相关接口")
@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    @Operation(summary = "新增记账", description = "记录一笔收入或支出，自动更新用户余额")
    @PostMapping
    public Result<TransactionVO> record(@RequestBody TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(request, transaction);
        transaction.setUserId(userId);

        Transaction saved = transactionService.record(transaction);

        return Result.success(convertToVO(saved));
    }

    @Operation(summary = "更新记账", description = "更新记账记录")
    @PutMapping("/{id}")
    public Result<TransactionVO> update(@PathVariable Long id, @RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(request, transaction);
        transaction.setId(id);
        transactionService.updateById(transaction);

        // 重新查询获取完整信息
        Transaction updated = transactionService.getById(id);
        return Result.success(convertToVO(updated));
    }

    @Operation(summary = "删除记账", description = "删除记账记录，自动回滚余额")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        log.info("删除记账记录, 收到id字符串: {}", id);
        Long longId = Long.parseLong(id);
        log.info("转换为Long: {}", longId);
        transactionService.deleteTransaction(longId);
        log.info("删除成功, id: {}", longId);
        return Result.success();
    }

    @Operation(summary = "获取记账详情", description = "根据ID获取记账详情")
    @GetMapping("/{id}")
    public Result<TransactionVO> getById(@PathVariable String id) {
        log.info("获取记账详情, 收到id字符串: {}", id);
        Long longId = Long.parseLong(id);
        log.info("转换为Long: {}", longId);
        Transaction transaction = transactionService.getById(longId);
        log.info("查询结果: {}", transaction);
        if (transaction == null) {
            log.warn("记录不存在, id: {}", longId);
            return Result.error("记录不存在");
        }

        return Result.success(convertToVO(transaction));
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
                .map(t -> convertToVO(t))
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

    /**
     * 将 Transaction 转换为 TransactionVO，包含分类名称
     */
    private TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);

        // 将ID转为字符串，避免JavaScript精度丢失
        vo.setId(String.valueOf(transaction.getId()));

        // 设置日期时间字符串格式
        if (transaction.getTransactionDate() != null) {
            vo.setTransactionDate(transaction.getTransactionDate().toString());
        }
        if (transaction.getTransactionTime() != null) {
            vo.setTransactionTime(transaction.getTransactionTime().toString());
        }
        if (transaction.getCreateTime() != null) {
            vo.setCreateTime(transaction.getCreateTime().toString());
        }

        // 查询并设置分类信息
        if (transaction.getCategoryId() != null) {
            Category category = categoryMapper.selectById(transaction.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
                vo.setCategoryIcon(category.getIcon());
            }
        }

        // 处理空值，避免显示"null"
        if (vo.getCategoryName() == null) {
            vo.setCategoryName("未分类");
        }
        if (vo.getRemark() == null) {
            vo.setRemark("");
        }

        return vo;
    }

    @Operation(summary = "收支统计", description = "统计指定时间段内的收支总额")
    @GetMapping("/statistics")
    public Result<TransactionStatisticsVO> statistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtils.getCurrentUserId();

        // type: 1=支出, 2=收入
        BigDecimal expense = transactionService.getStatistics(userId, 1,
                startDate != null ? java.time.LocalDate.parse(startDate) : null,
                endDate != null ? java.time.LocalDate.parse(endDate) : null);
        BigDecimal income = transactionService.getStatistics(userId, 2,
                startDate != null ? java.time.LocalDate.parse(startDate) : null,
                endDate != null ? java.time.LocalDate.parse(endDate) : null);

        TransactionStatisticsVO vo = new TransactionStatisticsVO();
        vo.setTotalIncome(income);
        vo.setTotalExpense(expense);
        vo.setBalance(income.subtract(expense));

        return Result.success(vo);
    }

    @Operation(summary = "分类统计", description = "按分类统计支出或收入，用于饼图展示")
    @GetMapping("/statistics/category")
    public Result<List<CategoryStatisticsVO>> categoryStatistics(
            @RequestParam Integer type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<CategoryStatisticsVO> list = transactionService.getCategoryStatistics(
                userId,
                type,
                startDate != null ? java.time.LocalDate.parse(startDate) : null,
                endDate != null ? java.time.LocalDate.parse(endDate) : null
        );

        return Result.success(list);
    }

    @Operation(summary = "月度趋势", description = "获取近N个月的收支趋势，用于折线图展示")
    @GetMapping("/statistics/trend")
    public Result<List<MonthlyTrendVO>> monthlyTrend(
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<MonthlyTrendVO> list = transactionService.getMonthlyTrend(userId, months);
        return Result.success(list);
    }

    @Operation(summary = "年度统计", description = "获取指定年份的年度统计汇总")
    @GetMapping("/statistics/yearly")
    public Result<Map<String, Object>> yearlyStatistics(
            @RequestParam(required = false) Integer year) {
        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> result = transactionService.getYearlyStatistics(userId, year);
        return Result.success(result);
    }
}
