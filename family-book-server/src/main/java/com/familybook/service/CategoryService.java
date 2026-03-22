package com.familybook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.familybook.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取用户分类列表
     */
    List<Category> getUserCategories(Long userId, Integer type);

    /**
     * 初始化系统分类
     */
    void initSystemCategories(Long userId);

    /**
     * 创建自定义分类
     */
    Category createCategory(Category category);
}
