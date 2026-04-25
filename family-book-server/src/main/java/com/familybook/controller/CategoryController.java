package com.familybook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familybook.common.Result;
import com.familybook.dto.request.CategoryRequest;
import com.familybook.entity.Category;
import com.familybook.entity.Transaction;
import com.familybook.security.SecurityUtils;
import com.familybook.service.CategoryService;
import com.familybook.service.TransactionService;
import com.familybook.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "分类管理", description = "收支分类的增删改查、系统分类初始化")
@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;

    @Operation(summary = "获取分类列表", description = "根据类型获取用户的分类列表，type: 1-支出, 2-收入")
    @GetMapping("/list")
    public Result<List<CategoryVO>> list(@RequestParam(required = false) Integer type) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Category> categories = categoryService.getUserCategories(userId, type);

        List<CategoryVO> voList = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "初始化系统分类", description = "为新用户创建预设的分类列表")
    @PostMapping("/init")
    public Result<Void> initSystemCategories() {
        Long userId = SecurityUtils.getCurrentUserId();
        categoryService.initSystemCategories(userId);
        return Result.success();
    }

    @Operation(summary = "创建分类", description = "创建自定义收支分类")
    @PostMapping
    public Result<CategoryVO> create(@RequestBody CategoryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Category category = new Category();
        BeanUtils.copyProperties(request, category);
        category.setUserId(userId);
        category.setIsSystem(0); // 自定义分类

        Category saved = categoryService.createCategory(category);

        return Result.success(convertToVO(saved));
    }

    @Operation(summary = "更新分类", description = "更新分类信息，只能更新自定义分类")
    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        if (isSystemCategory(category)) {
            return Result.error("系统预设分类不能编辑");
        }

        if (!canAccessCategory(userId, category)) {
            return Result.error("无权修改此分类");
        }

        BeanUtils.copyProperties(request, category);
        category.setId(id);

        categoryService.updateById(category);

        return Result.success(convertToVO(category));
    }

    @Operation(summary = "删除分类", description = "删除自定义分类，系统预设分类不能删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        if (isSystemCategory(category)) {
            return Result.error("系统预设分类不能删除");
        }

        if (!canAccessCategory(userId, category)) {
            return Result.error("无权删除此分类");
        }

        if (isCategoryUsedByTransactions(userId, id)) {
            return Result.error("该分类已被账单使用，无法删除");
        }

        categoryService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        if (!canAccessCategory(userId, category)) {
            return Result.error("无权查看此分类");
        }

        return Result.success(convertToVO(category));
    }

    private boolean isSystemCategory(Category category) {
        return category.getIsSystem() != null && category.getIsSystem() == 1;
    }

    private boolean canAccessCategory(Long userId, Category category) {
        return category.getUserId() == null || category.getUserId().equals(userId);
    }

    private boolean isCategoryUsedByTransactions(Long userId, Long categoryId) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
                .eq(Transaction::getCategoryId, categoryId);
        return transactionService.count(wrapper) > 0;
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        if (category.getId() != null) {
            vo.setId(String.valueOf(category.getId()));
        }
        return vo;
    }
}
