package com.familybook.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * 交易记录查询请求DTO
 * 用于分页查询记账记录
 */
@Data
public class TransactionQueryRequest {

    /**
     * 家庭组ID（查询家庭共享数据时使用）
     */
    private Long familyId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 类型：1-收入，2-支出
     */
    private Integer type;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认20
     */
    private Integer pageSize = 20;
}
