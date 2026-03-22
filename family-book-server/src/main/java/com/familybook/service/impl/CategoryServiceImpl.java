package com.familybook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Category;
import com.familybook.mapper.CategoryMapper;
import com.familybook.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> getUserCategories(Long userId, Integer type) {
        return null;
    }

    @Override
    public void initSystemCategories(Long userId) {

    }

    @Override
    public Category createCategory(Category category) {
        return null;
    }
}
