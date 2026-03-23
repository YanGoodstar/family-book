package com.familybook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.familybook.entity.Category;
import com.familybook.mapper.CategoryMapper;
import com.familybook.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        // 查询条件：系统预设分类 或 该用户的自定义分类
        wrapper.and(w -> w.isNull(Category::getUserId).or().eq(Category::getUserId, userId));

        if (type != null) {
            wrapper.eq(Category::getType, type);
        }

        // 按类型和排序字段排序
        wrapper.orderByAsc(Category::getType).orderByAsc(Category::getSort);

        return this.list(wrapper);
    }

    @Override
    public void initSystemCategories(Long userId) {
        // 预设支出分类
        List<Category> expenseCategories = new ArrayList<>();
        expenseCategories.add(createSystemCategory("餐饮", 2, "food", 1));
        expenseCategories.add(createSystemCategory("交通", 2, "transport", 2));
        expenseCategories.add(createSystemCategory("购物", 2, "shopping", 3));
        expenseCategories.add(createSystemCategory("娱乐", 2, "entertainment", 4));
        expenseCategories.add(createSystemCategory("居住", 2, "home", 5));
        expenseCategories.add(createSystemCategory("医疗", 2, "medical", 6));
        expenseCategories.add(createSystemCategory("教育", 2, "education", 7));
        expenseCategories.add(createSystemCategory("其他", 2, "other", 99));

        // 预设收入分类
        List<Category> incomeCategories = new ArrayList<>();
        incomeCategories.add(createSystemCategory("工资", 1, "salary", 1));
        incomeCategories.add(createSystemCategory("奖金", 1, "bonus", 2));
        incomeCategories.add(createSystemCategory("投资", 1, "investment", 3));
        incomeCategories.add(createSystemCategory("兼职", 1, "parttime", 4));
        incomeCategories.add(createSystemCategory("其他", 1, "other", 99));

        // 保存所有分类
        List<Category> allCategories = new ArrayList<>();
        allCategories.addAll(expenseCategories);
        allCategories.addAll(incomeCategories);

        for (Category category : allCategories) {
            category.setUserId(userId);
            this.save(category);
        }
    }

    @Override
    public Category createCategory(Category category) {
        // 设置排序（如果未指定）
        if (category.getSort() == null) {
            category.setSort(100);
        }
        this.save(category);
        return category;
    }

    /**
     * 创建系统预设分类
     */
    private Category createSystemCategory(String name, Integer type, String icon, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setSort(sort);
        category.setIsSystem(1); // 系统预设
        return category;
    }
}
