package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.ArticlePublishDTO;
import com.eaxon.coderhubpojo.VO.ArticleDetailVO;
import com.eaxon.coderhubpojo.VO.ArticleVO;
import com.eaxon.coderhubpojo.entity.Tag;
import com.eaxon.coderhubserver.service.ArticleService;
import com.eaxon.coderhubserver.mapper.TagMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器（用户端）
 */
@RestController
@RequestMapping("/article")
@Slf4j
@Api(tags = "文章相关接口")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private TagMapper tagMapper;

    /**
     * 发布文章
     */
    @PostMapping("/publish")
    @ApiOperation("发布文章")
    public Result<ArticleVO> publishArticle(@RequestBody ArticlePublishDTO dto) {
        log.info("发布文章：{}", dto.getTitle());
        ArticleVO vo = articleService.publishArticle(dto);
        return Result.success(vo);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{articleId}")
    @ApiOperation("获取文章详情")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable String articleId) {
        log.info("获取文章详情：articleId={}", articleId);
        ArticleDetailVO vo = articleService.getArticleDetail(articleId);
        return Result.success(vo);
    }

    /**
     * 获取文章列表
     */
    @GetMapping("/list")
    @ApiOperation("获取文章列表")
    public Result<List<ArticleVO>> getArticleList(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String tagId,
            @RequestParam(required = false) Integer status
    ) {
        log.info("获取文章列表：categoryId={}, tagId={}, status={}", categoryId, tagId, status);
        List<ArticleVO> list = articleService.getArticleList(categoryId, tagId, status);
        return Result.success(list);
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{articleId}")
    @ApiOperation("删除文章")
    public Result<String> deleteArticle(@PathVariable String articleId) {
        log.info("删除文章：articleId={}", articleId);
        articleService.deleteArticle(articleId);
        return Result.success("删除成功");
    }

    /**
     * 搜索标签（用于自动完成）
     */
    @GetMapping("/tags/search")
    @ApiOperation("搜索标签")
    public Result<List<Tag>> searchTags(@RequestParam String keyword) {
        log.info("搜索标签：keyword={}", keyword);
        List<Tag> tags = tagMapper.searchByKeyword(keyword);
        return Result.success(tags);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/tags/hot")
    @ApiOperation("获取热门标签")
    public Result<List<Tag>> getHotTags(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取热门标签：limit={}", limit);
        List<Tag> tags = tagMapper.getHotTags(limit);
        return Result.success(tags);
    }
}

