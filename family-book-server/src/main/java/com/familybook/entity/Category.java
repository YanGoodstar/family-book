package com.familybook.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类实体类
 * 用于管理收支分类，支持用户自定义分类和系统预设分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class Category extends BaseEntity {

    /**
     * 用户ID
     * 自定义分类关联的用户，系统预设分类为null
     */
    private Long userId;

    /**
     * 家庭组ID
     * 家庭共享分类关联的家庭组
     */
    private Long familyId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类类型：1=收入，2=支出
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

    /**
     * 是否系统预设：0=自定义，1=系统预设
     */
    private Integer isSystem;
}
