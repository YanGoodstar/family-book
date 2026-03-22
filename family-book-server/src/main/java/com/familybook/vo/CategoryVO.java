package com.familybook.vo;

import lombok.Data;

/**
 * 分类视图对象
 * 用于返回分类数据给前端
 */
@Data
public class CategoryVO {

    /**
     * 分类ID
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
    private Integer sortOrder;

    /**
     * 父分类ID，顶级分类为0
     */
    private Long parentId;

    /**
     * 是否系统预设：0-否，1-是
     */
    private Integer isSystem;

    /**
     * 创建时间
     */
    private String createTime;
}
