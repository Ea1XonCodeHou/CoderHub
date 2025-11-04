package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.NewTutorialDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
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
}
