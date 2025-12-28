package com.eaxon.coderhubserver.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.constant.StatusConstant;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.VO.CategoryVO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.entity.Document;
import com.eaxon.coderhubpojo.entity.Tutorial;
import com.eaxon.coderhubpojo.entity.TutorialChapter;
import com.eaxon.coderhubpojo.entity.Video;
import com.eaxon.coderhubserver.service.CategoryService;
import com.eaxon.coderhubserver.service.TutorialChapterService;
import com.eaxon.coderhubserver.service.TutorialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/tutorial")
@Slf4j
@Tag(name = "用户端教程接口")
public class TutorialController {

    @Autowired
    private TutorialService tutorialService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TutorialChapterService tutorialChapterService;

    /**
     * 分页查询教程列表（用户端）
     * 
     * @param page       页码，默认1
     * @param pageSize   每页数量，默认12
     * @param categoryId 分类ID（可选）
     * @return 教程分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询教程列表")
    public Result<PageResult<Tutorial>> page(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") Integer page,
            
            @Parameter(description = "每页数量", example = "12") 
            @RequestParam(defaultValue = "12") Integer pageSize,
            
            @Parameter(description = "分类ID", required = false) 
            @RequestParam(required = false) Long categoryId) {
        
        log.info("用户端分页查询教程：page={}, pageSize={}, categoryId={}", page, pageSize, categoryId);
        
        // 用户端只查询已发布的教程
        PageResult<Tutorial> pageResult = tutorialService.pageQuery(page, pageSize, StatusConstant.RELEASED, categoryId);
        
        return Result.success(pageResult);
    }

    /**
     * 获取所有分类列表
     * 
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有分类列表")
    public Result<List<CategoryVO>> getCategories() {
        log.info("用户端查询分类列表");
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 根据ID获取教程详情
     * 
     * @param id 教程ID
     * @return 教程详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取教程详情")
    public Result<Tutorial> getById(
            @Parameter(description = "教程ID", required = true) 
            @PathVariable String id) {
        log.info("用户端查询教程详情：id={}", id);
        Tutorial tutorial = tutorialService.getById(id);
        return Result.success(tutorial);
    }

    /**
     * 获取教程的章节列表
     * 
     * @param tutorialId 教程ID
     * @return 章节列表
     */
    @GetMapping("/{tutorialId}/chapters")
    @Operation(summary = "获取教程章节列表")
    public Result<List<TutorialChapter>> getChapters(
            @Parameter(description = "教程ID", required = true) 
            @PathVariable String tutorialId) {
        log.info("用户端查询教程章节：tutorialId={}", tutorialId);
        List<TutorialChapter> chapters = tutorialChapterService.getChaptersByTutorialId(tutorialId);
        return Result.success(chapters);
    }

    /**
     * 获取章节的文档列表
     * 
     * @param chapterId 章节ID
     * @return 文档列表
     */
    @GetMapping("/chapter/{chapterId}/documents")
    @Operation(summary = "获取章节文档列表")
    public Result<List<Document>> getChapterDocuments(
            @Parameter(description = "章节ID", required = true) 
            @PathVariable String chapterId) {
        log.info("用户端查询章节文档：chapterId={}", chapterId);
        List<Document> documents = tutorialChapterService.getDocumentsByChapterId(chapterId);
        return Result.success(documents);
    }

    /**
     * 获取章节的视频列表
     * 
     * @param chapterId 章节ID
     * @return 视频列表
     */
    @GetMapping("/chapter/{chapterId}/videos")
    @Operation(summary = "获取章节视频列表")
    public Result<List<Video>> getChapterVideos(
            @Parameter(description = "章节ID", required = true) 
            @PathVariable String chapterId) {
        log.info("用户端查询章节视频：chapterId={}", chapterId);
        List<Video> videos = tutorialChapterService.getVideosByChapterId(chapterId);
        return Result.success(videos);
    }
}
