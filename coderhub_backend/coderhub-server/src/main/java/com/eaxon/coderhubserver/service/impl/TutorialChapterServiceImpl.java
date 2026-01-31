package com.eaxon.coderhubserver.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaxon.coderhubcommon.utils.AliOssUtil;
import com.eaxon.coderhubpojo.DTO.NewChapterDTO;
import com.eaxon.coderhubpojo.DTO.UploadDocumentDTO;
import com.eaxon.coderhubpojo.DTO.UploadVideoDTO;
import com.eaxon.coderhubpojo.entity.Document;
import com.eaxon.coderhubpojo.entity.TutorialChapter;
import com.eaxon.coderhubpojo.entity.Video;
import com.eaxon.coderhubserver.mapper.DocumentMapper;
import com.eaxon.coderhubserver.mapper.TutorialChapterMapper;
import com.eaxon.coderhubserver.mapper.VideoMapper;
import com.eaxon.coderhubserver.service.TutorialChapterService;

import lombok.extern.slf4j.Slf4j;

/**
 * 教程章节服务实现
 */
@Service
@Slf4j
public class TutorialChapterServiceImpl implements TutorialChapterService {

    @Autowired
    private TutorialChapterMapper tutorialChapterMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 创建新章节
     */
    @Override
    @Transactional
    public TutorialChapter createChapter(NewChapterDTO newChapterDTO) {
        log.info("创建新章节：{}", newChapterDTO);

        // 构建章节实体
        TutorialChapter chapter = new TutorialChapter();
        BeanUtils.copyProperties(newChapterDTO, chapter);
        
        // 设置ID和时间
        chapter.setId(UUID.randomUUID().toString());
        chapter.setCreateTime(LocalDateTime.now());
        chapter.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        tutorialChapterMapper.insertChapter(chapter);

        // 更新教程的章节数量
        tutorialChapterMapper.updateTutorialChapterCount(newChapterDTO.getTutorialId());

        log.info("章节创建成功，章节ID：{}", chapter.getId());
        return chapter;
    }

    /**
     * 上传文档到章节
     */
    @Override
    @Transactional
    public Document uploadDocument(UploadDocumentDTO uploadDocumentDTO) {
        log.info("上传文档到章节：{}", uploadDocumentDTO);

        // 验证章节是否存在
        TutorialChapter chapter = tutorialChapterMapper.selectById(uploadDocumentDTO.getChapterId());
        if (chapter == null) {
            throw new RuntimeException("章节不存在");
        }

        // 构建文档实体
        Document document = new Document();
        BeanUtils.copyProperties(uploadDocumentDTO, document);
        
        // 设置ID和时间
        document.setId(UUID.randomUUID().toString());
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        documentMapper.insertDocument(document);

        log.info("文档上传成功，文档ID：{}", document.getId());
        return document;
    }

    /**
     * 上传视频到章节
     */
    @Override
    @Transactional
    public Video uploadVideo(UploadVideoDTO uploadVideoDTO) {
        log.info("上传视频到章节：{}", uploadVideoDTO);

        // 验证章节是否存在
        TutorialChapter chapter = tutorialChapterMapper.selectById(uploadVideoDTO.getChapterId());
        if (chapter == null) {
            throw new RuntimeException("章节不存在");
        }

        // 构建视频实体
        Video video = new Video();
        BeanUtils.copyProperties(uploadVideoDTO, video);
        
        // 设置ID和时间
        video.setId(UUID.randomUUID().toString());
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        videoMapper.insertVideo(video);

        log.info("视频上传成功，视频ID：{}", video.getId());
        return video;
    }

    /**
     * 查询教程的章节列表
     */
    @Override
    public List<TutorialChapter> getChaptersByTutorialId(String tutorialId) {
        log.info("查询教程章节列表，教程ID：{}", tutorialId);
        return tutorialChapterMapper.selectByTutorialId(tutorialId);
    }

    /**
     * 查询章节的文档列表
     */
    @Override
    public List<Document> getDocumentsByChapterId(String chapterId) {
        log.info("查询章节文档列表，章节ID：{}", chapterId);
        return documentMapper.selectByChapterId(chapterId);
    }

    /**
     * 查询章节的视频列表
     */
    @Override
    public List<Video> getVideosByChapterId(String chapterId) {
        log.info("查询章节视频列表，章节ID：{}", chapterId);
        return videoMapper.selectByChapterId(chapterId);
    }

    /**
     * 删除文档
     */
    @Override
    @Transactional
    public void deleteDocument(String id) {
        log.info("删除文档，文档ID：{}", id);

        // 查询文档信息
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        // 从OSS删除文件
        try {
            if (document.getDocumentUrl() != null && !document.getDocumentUrl().isEmpty()) {
                // 从完整URL中提取文件路径
                // 例如: https://bucket.oss-cn-hangzhou.aliyuncs.com/tutorial/doc/xxx.pdf
                // 提取: tutorial/doc/xxx.pdf
                String fileUrl = document.getDocumentUrl();
                int lastSlashIndex = fileUrl.indexOf(".com/");
                if (lastSlashIndex != -1) {
                    String objectName = fileUrl.substring(lastSlashIndex + 5); // 跳过 ".com/"
                    log.info("从OSS删除文档文件：{}", objectName);
                    aliOssUtil.delete(objectName);
                }
            }
        } catch (Exception e) {
            log.error("删除OSS文件失败：{}", e.getMessage());
            // 即使OSS删除失败，也继续删除数据库记录
        }

        // 从数据库删除
        documentMapper.deleteById(id);
        log.info("文档删除成功");
    }

    /**
     * 删除视频
     */
    @Override
    @Transactional
    public void deleteVideo(String id) {
        log.info("删除视频，视频ID：{}", id);

        // 查询视频信息
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }

        // 从OSS删除文件
        try {
            if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty()) {
                // 从完整URL中提取文件路径
                String videoUrl = video.getVideoUrl();
                int lastSlashIndex = videoUrl.indexOf(".com/");
                if (lastSlashIndex != -1) {
                    String objectName = videoUrl.substring(lastSlashIndex + 5);
                    log.info("从OSS删除视频文件：{}", objectName);
                    aliOssUtil.delete(objectName);
                }
            }
        } catch (Exception e) {
            log.error("删除OSS文件失败：{}", e.getMessage());
            // 即使OSS删除失败，也继续删除数据库记录
        }

        // 从数据库删除
        videoMapper.deleteById(id);
        log.info("视频删除成功");
    }

    /**
     * 更新章节信息
     */
    @Override
    @Transactional
    public void updateChapter(TutorialChapter chapter) {
        log.info("更新章节信息：{}", chapter);

        // 验证章节是否存在
        TutorialChapter existingChapter = tutorialChapterMapper.selectById(chapter.getId());
        if (existingChapter == null) {
            throw new RuntimeException("章节不存在");
        }

        // 更新时间
        chapter.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        tutorialChapterMapper.updateChapter(chapter);
        log.info("章节更新成功");
    }
}
