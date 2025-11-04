package com.eaxon.coderhubserver.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.NewChapterDTO;
import com.eaxon.coderhubpojo.DTO.NewTutorialDTO;
import com.eaxon.coderhubpojo.DTO.UploadDocumentDTO;
import com.eaxon.coderhubpojo.DTO.UploadVideoDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.entity.Document;
import com.eaxon.coderhubpojo.entity.Tutorial;
import com.eaxon.coderhubpojo.entity.TutorialChapter;
import com.eaxon.coderhubpojo.entity.Video;
import com.eaxon.coderhubserver.service.TutorialChapterService;
import com.eaxon.coderhubserver.service.TutorialService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/tutorial")
@Slf4j
@Api(tags = "管理端-教程管理接口")
public class TutorialManagerController {
    @Autowired
    private TutorialService tutorialService;
    
    @Autowired
    private TutorialChapterService tutorialChapterService;
    
    /**
     * 分页查询教程列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param status 状态：0-草稿 1-已发布 2-已下架（可选）
     * @param categoryId 分类ID（可选）
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页查询教程列表")
    public Result<PageResult<Tutorial>> getTutorialList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId) {
        log.info("分页查询教程列表：page={}, pageSize={}, status={}, categoryId={}", 
                page, pageSize, status, categoryId);
        PageResult<Tutorial> pageResult = tutorialService.pageQuery(page, pageSize, status, categoryId);
        return Result.success(pageResult);
    }
    
    /**
     * 新增课程
     * @param newTutorialDTO
     * @return
     */
    @PostMapping("/create")
    @ApiOperation(value = "新增教程接口")
    public Result<Tutorial> createNewTutorial(@RequestBody NewTutorialDTO newTutorialDTO){
        log.info("新增教程：{}", newTutorialDTO);
        Tutorial tutorial = tutorialService.createNewTutorial(newTutorialDTO);
        return Result.success(tutorial);
    }
    
    // ==================== 章节管理接口 ====================
    
    /**
     * 新增章节
     * @param newChapterDTO 章节信息
     * @return 创建的章节
     */
    @PostMapping("/chapter/create")
    @ApiOperation(value = "新增章节")
    public Result<TutorialChapter> createChapter(@RequestBody NewChapterDTO newChapterDTO) {
        log.info("新增章节：{}", newChapterDTO);
        TutorialChapter chapter = tutorialChapterService.createChapter(newChapterDTO);
        return Result.success(chapter);
    }
    
    /**
     * 查询教程的章节列表
     * @param tutorialId 教程ID
     * @return 章节列表
     */
    @GetMapping("/chapter/list")
    @ApiOperation(value = "查询教程章节列表")
    public Result<List<TutorialChapter>> getChapterList(@RequestParam String tutorialId) {
        log.info("查询教程章节列表，教程ID：{}", tutorialId);
        List<TutorialChapter> chapters = tutorialChapterService.getChaptersByTutorialId(tutorialId);
        return Result.success(chapters);
    }
    
    /**
     * 上传文档到章节
     * @param uploadDocumentDTO 文档信息
     * @return 创建的文档
     */
    @PostMapping("/document/upload")
    @ApiOperation(value = "上传文档到章节")
    public Result<Document> uploadDocument(@RequestBody UploadDocumentDTO uploadDocumentDTO) {
        log.info("上传文档：{}", uploadDocumentDTO);
        Document document = tutorialChapterService.uploadDocument(uploadDocumentDTO);
        return Result.success(document);
    }
    
    /**
     * 查询章节的文档列表
     * @param chapterId 章节ID
     * @return 文档列表
     */
    @GetMapping("/document/list")
    @ApiOperation(value = "查询章节文档列表")
    public Result<List<Document>> getDocumentList(@RequestParam String chapterId) {
        log.info("查询章节文档列表，章节ID：{}", chapterId);
        List<Document> documents = tutorialChapterService.getDocumentsByChapterId(chapterId);
        return Result.success(documents);
    }
    
    /**
     * 上传视频到章节
     * @param uploadVideoDTO 视频信息
     * @return 创建的视频
     */
    @PostMapping("/video/upload")
    @ApiOperation(value = "上传视频到章节")
    public Result<Video> uploadVideo(@RequestBody UploadVideoDTO uploadVideoDTO) {
        log.info("上传视频：{}", uploadVideoDTO);
        Video video = tutorialChapterService.uploadVideo(uploadVideoDTO);
        return Result.success(video);
    }
    
    /**
     * 查询章节的视频列表
     * @param chapterId 章节ID
     * @return 视频列表
     */
    @GetMapping("/video/list")
    @ApiOperation(value = "查询章节视频列表")
    public Result<List<Video>> getVideoList(@RequestParam String chapterId) {
        log.info("查询章节视频列表，章节ID：{}", chapterId);
        List<Video> videos = tutorialChapterService.getVideosByChapterId(chapterId);
        return Result.success(videos);
    }
}
