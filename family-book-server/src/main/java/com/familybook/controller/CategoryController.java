package com.familybook.controller;

import com.familybook.common.Result;
import com.familybook.dto.request.CategoryRequest;
import com.familybook.entity.Category;
import com.familybook.security.SecurityUtils;
import com.familybook.service.CategoryService;
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

    @Operation(summary = "获取分类列表", description = "根据类型获取用户的分类列表，type: 1-支出, 2-收入")
    @GetMapping("/list")
    public Result<List<CategoryVO>> list(@RequestParam(required = false) Integer type) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Category> categories = categoryService.getUserCategories(userId, type);

        List<CategoryVO> voList = categories.stream()
                .map(c -> {
                    CategoryVO vo = new CategoryVO();
                    BeanUtils.copyProperties(c, vo);
                    return vo;
                })
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

        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(saved, vo);
        return Result.success(vo);
    }

    @Operation(summary = "更新分类", description = "更新分类信息，只能更新自定义分类")
    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        Category category = new Category();
        BeanUtils.copyProperties(request, category);
        category.setId(id);

        categoryService.updateById(category);

        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return Result.success(vo);
    }

    @Operation(summary = "删除分类", description = "删除自定义分类，系统预设分类不能删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        if (category.getIsSystem() != null && category.getIsSystem() == 1) {
            return Result.error("系统预设分类不能删除");
        }

        categoryService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return Result.success(vo);
    }
}
