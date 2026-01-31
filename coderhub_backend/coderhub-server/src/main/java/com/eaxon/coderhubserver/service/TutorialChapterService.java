package com.eaxon.coderhubserver.service;

import java.util.List;

import com.eaxon.coderhubpojo.DTO.NewChapterDTO;
import com.eaxon.coderhubpojo.DTO.UploadDocumentDTO;
import com.eaxon.coderhubpojo.DTO.UploadVideoDTO;
import com.eaxon.coderhubpojo.entity.Document;
import com.eaxon.coderhubpojo.entity.TutorialChapter;
import com.eaxon.coderhubpojo.entity.Video;

/**
 * 教程章节服务接口
 */
public interface TutorialChapterService {

    /**
     * 创建新章节
     * @param newChapterDTO 章节信息
     * @return 创建的章节
     */
    TutorialChapter createChapter(NewChapterDTO newChapterDTO);

    /**
     * 上传文档到章节
     * @param uploadDocumentDTO 文档信息
     * @return 创建的文档
     */
    Document uploadDocument(UploadDocumentDTO uploadDocumentDTO);

    /**
     * 上传视频到章节
     * @param uploadVideoDTO 视频信息
     * @return 创建的视频
     */
    Video uploadVideo(UploadVideoDTO uploadVideoDTO);

    /**
     * 查询教程的章节列表
     * @param tutorialId 教程ID
     * @return 章节列表
     */
    List<TutorialChapter> getChaptersByTutorialId(String tutorialId);

    /**
     * 查询章节的文档列表
     * @param chapterId 章节ID
     * @return 文档列表
     */
    List<Document> getDocumentsByChapterId(String chapterId);

    /**
     * 查询章节的视频列表
     * @param chapterId 章节ID
     * @return 视频列表
     */
    List<Video> getVideosByChapterId(String chapterId);

    /**
     * 删除文档
     * @param id 文档ID
     */
    void deleteDocument(String id);

    /**
     * 删除视频
     * @param id 视频ID
     */
    void deleteVideo(String id);

    /**
     * 更新章节信息
     * @param chapter 章节信息
     */
    void updateChapter(TutorialChapter chapter);
}
