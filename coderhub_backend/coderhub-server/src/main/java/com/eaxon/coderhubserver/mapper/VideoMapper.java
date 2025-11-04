package com.eaxon.coderhubserver.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.Video;

/**
 * 视频Mapper
 */
@Mapper
public interface VideoMapper {

    /**
     * 插入视频
     * @param video 视频信息
     */
    void insertVideo(Video video);

    /**
     * 根据章节ID查询视频列表
     * @param chapterId 章节ID
     * @return 视频列表
     */
    List<Video> selectByChapterId(@Param("chapterId") String chapterId);

    /**
     * 根据视频ID删除视频
     * @param videoId 视频ID
     */
    void deleteById(@Param("videoId") String videoId);
}
