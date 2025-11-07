package com.eaxon.coderhubserver.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.VO.CategoryVO;
import com.eaxon.coderhubserver.service.CategoryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户端-分类接口
 */
@RestController
@RequestMapping("/category")
@Slf4j
@Api(tags = "用户端-分类接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有已启用的分类（包含文章数统计）
     */
    @GetMapping("/list")
    @ApiOperation("获取所有分类列表")
    public Result<List<CategoryVO>> getAllCategories() {
        log.info("用户端-查询所有分类");
        List<CategoryVO> categories = categoryService.getAllCategories();
        // 只返回已启用的分类
        List<CategoryVO> enabledCategories = categories.stream()
                .filter(c -> c.getStatus() == 1)
                .toList();
        return Result.success(enabledCategories);
    }
}
