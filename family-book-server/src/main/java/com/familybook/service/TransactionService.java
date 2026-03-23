package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Transaction;
import com.familybook.vo.CategoryStatisticsVO;
import com.familybook.vo.MonthlyTrendVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 交易记录服务接口
 */
public interface TransactionService extends IService<Transaction> {

    /**
     * 记账
     */
    Transaction record(Transaction transaction);

    /**
     * 获取交易列表
     */
    List<Transaction> getTransactions(Long userId, Long familyId, LocalDate startDate, LocalDate endDate, Integer type, Long categoryId);

    /**
     * 获取收支统计
     */
    BigDecimal getStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate);

    /**
     * 删除交易记录并回滚余额
     */
    void deleteTransaction(Long id);

    /**
     * 获取分类统计（按分类汇总支出或收入）
     */
    List<CategoryStatisticsVO> getCategoryStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate);

    /**
     * 获取月度趋势（近N个月的收支趋势）
     */
    List<MonthlyTrendVO> getMonthlyTrend(Long userId, Integer months);

    /**
     * 获取年度统计
     */
    Map<String, Object> getYearlyStatistics(Long userId, Integer year);
}
