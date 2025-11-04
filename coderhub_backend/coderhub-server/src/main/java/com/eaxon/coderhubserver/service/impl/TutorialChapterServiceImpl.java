package com.eaxon.coderhubserver.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
