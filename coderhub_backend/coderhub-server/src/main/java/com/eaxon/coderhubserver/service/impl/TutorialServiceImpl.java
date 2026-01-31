package com.eaxon.coderhubserver.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaxon.coderhubcommon.constant.StatusConstant;
import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubpojo.DTO.NewTutorialDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.VO.TutorialDeleteResult;
import com.eaxon.coderhubpojo.entity.Document;
import com.eaxon.coderhubpojo.entity.Tutorial;
import com.eaxon.coderhubpojo.entity.TutorialChapter;
import com.eaxon.coderhubpojo.entity.Video;
import com.eaxon.coderhubserver.mapper.DocumentMapper;
import com.eaxon.coderhubserver.mapper.TutorialChapterMapper;
import com.eaxon.coderhubserver.mapper.TutorialMapper;
import com.eaxon.coderhubserver.mapper.VideoMapper;
import com.eaxon.coderhubserver.service.TutorialService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TutorialServiceImpl implements TutorialService {

    @Autowired
    private TutorialMapper tutorialMapper;
    
    @Autowired
    private TutorialChapterMapper tutorialChapterMapper;
    
    @Autowired
    private DocumentMapper documentMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private AliOssUtil aliOssUtil;
    
    /**
     * 新增课程
     * @param newTutorialDTO
     * @return
     */
    @Override
    public Tutorial createNewTutorial(NewTutorialDTO newTutorialDTO) {
        Tutorial tutorial = new Tutorial();
        BeanUtils.copyProperties(newTutorialDTO, tutorial);
        
        // 设置ID和初始值
        tutorial.setId(String.valueOf(UUID.randomUUID()));
        tutorial.setStatus(StatusConstant.RELEASED);
        tutorial.setViewCount(Long.valueOf(StatusConstant.NO_COUNT));
        tutorial.setLikeCount(StatusConstant.NO_COUNT);
        tutorial.setRecommendCount(StatusConstant.NO_COUNT);
        tutorial.setStudentCount(StatusConstant.NO_COUNT);
        tutorial.setRating(BigDecimal.valueOf(StatusConstant.NO_COUNT));
        tutorial.setChapterCount(StatusConstant.NO_COUNT);
        tutorial.setCreateUser(BaseContext.getCurrentId());
        
        // 插入数据库
        tutorialMapper.insertTutorial(tutorial);
        
        log.info("新增教程成功：{}", tutorial.getId());
        return tutorial;
    }
    
    /**
     * 分页查询教程列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param status 状态过滤（可选）
     * @param categoryId 分类过滤（可选）
     * @return
     */
    @Override
    public PageResult<Tutorial> pageQuery(Integer page, Integer pageSize, Integer status, Long categoryId) {
        log.info("分页查询教程：page={}, pageSize={}, status={}, categoryId={}", 
                page, pageSize, status, categoryId);
        
        // 使用PageHelper进行分页
        PageHelper.startPage(page, pageSize);
        
        // 查询数据
        Page<Tutorial> tutorialPage = tutorialMapper.pageQuery(status, categoryId);
        
        // 封装分页结果
        PageResult<Tutorial> pageResult = new PageResult<>();
        pageResult.setTotal(tutorialPage.getTotal());
        pageResult.setRecords(tutorialPage.getResult());
        
        log.info("查询到{}条教程记录", tutorialPage.getTotal());
        return pageResult;
    }
    
    /**
     * 根据ID查询教程详情
     * @param id 教程ID
     * @return 教程信息
     */
    @Override
    public Tutorial getById(String id) {
        log.info("查询教程详情：id={}", id);
        Tutorial tutorial = tutorialMapper.selectById(id);
        if (tutorial == null) {
            log.warn("教程不存在：id={}", id);
        }
        return tutorial;
    }
    
    /**
     * 更新教程信息
     * @param tutorial 教程信息
     */
    @Override
    @Transactional
    public void updateTutorial(Tutorial tutorial) {
        log.info("更新教程：id={}", tutorial.getId());
        
        // 验证教程是否存在
        Tutorial existingTutorial = tutorialMapper.selectById(tutorial.getId());
        if (existingTutorial == null) {
            throw new RuntimeException("教程不存在");
        }
        
        // 更新时间
        tutorial.setUpdateTime(LocalDateTime.now());
        
        // 更新数据库
        tutorialMapper.updateTutorial(tutorial);
        log.info("教程更新成功");
    }
    
    /**
     * 删除教程（级联删除章节、文档、视频）
     * @param id 教程ID
     * @return 删除结果详情
     */
    @Override
    @Transactional
    public TutorialDeleteResult deleteTutorial(String id) {
        log.info("删除教程：id={}", id);
        
        // 验证教程是否存在
        Tutorial tutorial = tutorialMapper.selectById(id);
        if (tutorial == null) {
            throw new RuntimeException("教程不存在");
        }
        
        // 初始化统计数据
        int chapterCount = 0;
        int documentCount = 0;
        int videoCount = 0;
        int ossFileCount = 0;
        long totalFileSize = 0L;
        
        // 查询教程的所有章节
        List<TutorialChapter> chapters = tutorialChapterMapper.selectByTutorialId(id);
        chapterCount = chapters.size();
        log.info("教程《{}》共有 {} 个章节", tutorial.getTitle(), chapterCount);
        
        // 删除每个章节的文档和视频
        for (TutorialChapter chapter : chapters) {
            String chapterId = chapter.getId();
            log.info("开始删除章节：{} - {}", chapter.getChapterOrder(), chapter.getChapterTitle());
            
            // 删除章节的文档
            List<Document> documents = documentMapper.selectByChapterId(chapterId);
            documentCount += documents.size();
            for (Document doc : documents) {
                // 删除OSS文件
                try {
                    if (doc.getDocumentUrl() != null && !doc.getDocumentUrl().isEmpty()) {
                        String fileUrl = doc.getDocumentUrl();
                        int lastSlashIndex = fileUrl.indexOf(".com/");
                        if (lastSlashIndex != -1) {
                            String objectName = fileUrl.substring(lastSlashIndex + 5);
                            aliOssUtil.delete(objectName);
                            ossFileCount++;
                            if (doc.getFileSize() != null) {
                                totalFileSize += doc.getFileSize();
                            }
                            log.info("删除文档OSS文件：{} ({})", doc.getDocumentTitle(), objectName);
                        }
                    }
                } catch (Exception e) {
                    log.error("删除文档OSS文件失败：{}", e.getMessage());
                }
                // 删除数据库记录
                documentMapper.deleteById(doc.getId());
            }
            
            // 删除章节的视频
            List<Video> videos = videoMapper.selectByChapterId(chapterId);
            videoCount += videos.size();
            for (Video video : videos) {
                // 删除OSS文件
                try {
                    if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty()) {
                        String videoUrl = video.getVideoUrl();
                        int lastSlashIndex = videoUrl.indexOf(".com/");
                        if (lastSlashIndex != -1) {
                            String objectName = videoUrl.substring(lastSlashIndex + 5);
                            aliOssUtil.delete(objectName);
                            ossFileCount++;
                            if (video.getFileSize() != null) {
                                totalFileSize += video.getFileSize();
                            }
                            log.info("删除视频OSS文件：{} ({})", video.getVideoTitle(), objectName);
                        }
                    }
                } catch (Exception e) {
                    log.error("删除视频OSS文件失败：{}", e.getMessage());
                }
                // 删除数据库记录
                videoMapper.deleteById(video.getId());
            }
            
            // 删除章节
            tutorialChapterMapper.deleteById(chapterId);
            log.info("章节删除完成：{}", chapter.getChapterTitle());
        }
        
        // 删除教程封面图（如果存在）
        try {
            if (tutorial.getCoverImage() != null && !tutorial.getCoverImage().isEmpty()) {
                String coverUrl = tutorial.getCoverImage();
                int lastSlashIndex = coverUrl.indexOf(".com/");
                if (lastSlashIndex != -1) {
                    String objectName = coverUrl.substring(lastSlashIndex + 5);
                    aliOssUtil.delete(objectName);
                    ossFileCount++;
                    log.info("删除教程封面：{}", objectName);
                }
            }
        } catch (Exception e) {
            log.error("删除教程封面失败：{}", e.getMessage());
        }
        
        // 删除教程
        tutorialMapper.deleteById(id);
        
        log.info("教程删除成功！统计信息 - 章节数: {}, 文档数: {}, 视频数: {}, OSS文件数: {}, 总文件大小: {} 字节", 
                chapterCount, documentCount, videoCount, ossFileCount, totalFileSize);
        
        // 构建删除结果
        return TutorialDeleteResult.builder()
                .tutorialTitle(tutorial.getTitle())
                .chapterCount(chapterCount)
                .documentCount(documentCount)
                .videoCount(videoCount)
                .ossFileCount(ossFileCount)
                .totalFileSize(totalFileSize)
                .success(true)
                .message(String.format("成功删除教程《%s》及其 %d 个章节、%d 个文档、%d 个视频，共清理 %d 个OSS文件", 
                        tutorial.getTitle(), chapterCount, documentCount, videoCount, ossFileCount))
                .build();
    }
}
