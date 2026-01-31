package com.eaxon.coderhubpojo.VO;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程删除结果VO（返回级联删除的详细信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialDeleteResult implements Serializable {
    /**
     * 教程标题
     */
    private String tutorialTitle;
    
    /**
     * 删除的章节数量
     */
    private Integer chapterCount;
    
    /**
     * 删除的文档数量
     */
    private Integer documentCount;
    
    /**
     * 删除的视频数量
     */
    private Integer videoCount;
    
    /**
     * 删除的OSS文件数量
     */
    private Integer ossFileCount;
    
    /**
     * 删除的总文件大小（字节）
     */
    private Long totalFileSize;
    
    /**
     * 删除操作是否成功
     */
    private Boolean success;
    
    /**
     * 删除信息描述
     */
    private String message;
}
