package com.eaxon.coderhubserver.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubpojo.entity.Document;

/**
 * 文档Mapper
 */
@Mapper
public interface DocumentMapper {

    /**
     * 插入文档
     * @param document 文档信息
     */
    void insertDocument(Document document);

    /**
     * 根据章节ID查询文档列表
     * @param chapterId 章节ID
     * @return 文档列表
     */
    List<Document> selectByChapterId(@Param("chapterId") String chapterId);

    /**
     * 根据文档ID查询文档
     * @param id 文档ID
     * @return 文档信息
     */
    Document selectById(@Param("id") String id);

    /**
     * 根据文档ID删除文档
     * @param documentId 文档ID
     */
    void deleteById(@Param("documentId") String documentId);
}
