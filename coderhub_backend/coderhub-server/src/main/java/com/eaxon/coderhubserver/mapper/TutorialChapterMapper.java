package com.eaxon.coderhubserver.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.TutorialChapter;

/**
 * 教程章节Mapper
 */
@Mapper
public interface TutorialChapterMapper {

    /**
     * 插入章节
     * @param chapter 章节信息
     */
    void insertChapter(TutorialChapter chapter);

    /**
     * 根据教程ID查询章节列表
     * @param tutorialId 教程ID
     * @return 章节列表
     */
    List<TutorialChapter> selectByTutorialId(@Param("tutorialId") String tutorialId);

    /**
     * 根据章节ID查询章节
     * @param chapterId 章节ID
     * @return 章节信息
     */
    TutorialChapter selectById(@Param("chapterId") String chapterId);

    /**
     * 更新教程的章节数量
     * @param tutorialId 教程ID
     */
    void updateTutorialChapterCount(@Param("tutorialId") String tutorialId);
}
