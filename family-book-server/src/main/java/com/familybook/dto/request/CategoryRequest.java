package com.familybook.dto.request;

import lombok.Data;

/**
 * 分类请求DTO
 * 用于创建和更新收支分类
 */
@Data
public class CategoryRequest {

    /**
     * 分类ID，更新时传入
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类类型：1-支出，2-收入
     */
    private Integer type;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 排序顺序
     */
    private Integer sort;
}
