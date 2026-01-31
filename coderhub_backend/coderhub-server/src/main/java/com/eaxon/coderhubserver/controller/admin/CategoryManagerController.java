package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.CategoryDTO;
import com.eaxon.coderhubpojo.VO.CategoryVO;
import com.eaxon.coderhubserver.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Tag(name = "管理端-分类管理接口")
public class CategoryManagerController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有分类")
    public Result<List<CategoryVO>> getAllCategories() {
        log.info("查询所有分类");
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 查询一级分类
     */
    @GetMapping("/root")
    @Operation(summary = "查询一级分类")
    public Result<List<CategoryVO>> getRootCategories() {
        log.info("查询一级分类");
        List<CategoryVO> categories = categoryService.getRootCategories();
        return Result.success(categories);
    }

    /**
     * 根据父ID查询子分类
     */
    @GetMapping("/sub/{parentId}")
    @Operation(summary = "查询子分类")
    public Result<List<CategoryVO>> getSubCategories(@PathVariable String parentId) {
        log.info("查询子分类：parentId={}", parentId);
        List<CategoryVO> categories = categoryService.getSubCategories(parentId);
        return Result.success(categories);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询分类")
    public Result<CategoryVO> getCategoryById(@PathVariable String id) {
        log.info("查询分类详情：id={}", id);
        CategoryVO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    @Operation(summary = "新增分类")
    public Result<CategoryVO> addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        CategoryVO category = categoryService.addCategory(categoryDTO);
        log.info("分类新增成功：{}", category);
        return Result.success(category);
    }

    /**
     * 更新分类
     */
    @PutMapping
    @Operation(summary = "更新分类")
    public Result<CategoryVO> updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("更新分类：{}", categoryDTO);
        CategoryVO category = categoryService.updateCategory(categoryDTO);
        log.info("分类更新成功：{}", category);
        return Result.success(category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<String> deleteCategory(@PathVariable String id) {
        log.info("删除分类：id={}", id);
        categoryService.deleteCategory(id);
        log.info("分类删除成功");
        return Result.success("分类删除成功");
    }

    /**
     * 修改分类状态
     */
    @PutMapping("/status/{id}/{status}")
    @Operation(summary = "修改分类状态")
    public Result<String> updateStatus(@PathVariable String id, @PathVariable Integer status) {
        log.info("修改分类状态：id={}, status={}", id, status);
        categoryService.updateStatus(id, status);
        String message = status == 1 ? "分类已启用" : "分类已禁用";
        log.info(message);
        return Result.success(message);
    }
}
