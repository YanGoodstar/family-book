package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}
