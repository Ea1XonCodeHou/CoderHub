package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.NewTutorialDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.VO.TutorialDeleteResult;
import com.eaxon.coderhubpojo.entity.Tutorial;

public interface TutorialService {
    /**
     * 创建新教程
     */
    Tutorial createNewTutorial(NewTutorialDTO newTutorialDTO);
    
    /**
     * 分页查询教程列表
     */
    PageResult<Tutorial> pageQuery(Integer page, Integer pageSize, Integer status, Long categoryId);
    
    /**
     * 根据ID查询教程详情
     */
    Tutorial getById(String id);
    
    /**
     * 更新教程信息
     */
    void updateTutorial(Tutorial tutorial);
    
    /**
     * 删除教程（级联删除章节、文档、视频）
     * @return 删除结果详情
     */
    TutorialDeleteResult deleteTutorial(String id);
}
