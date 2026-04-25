package com.familybook.dto.request;

import lombok.Data;

/**
 * 梦想目标归档请求
 */
@Data
public class DreamGoalArchiveRequest {

    /**
     * 是否在归档时自动生成一笔支出
     */
    private Boolean createExpense;

    /**
     * 自动生成支出时使用的支出分类ID
     */
    private Long expenseCategoryId;
}
