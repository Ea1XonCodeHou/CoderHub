package com.eaxon.coderhubserver.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubcommon.constant.StatusConstant;
import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubpojo.DTO.NewTutorialDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.entity.Tutorial;
import com.eaxon.coderhubserver.mapper.TutorialMapper;
import com.eaxon.coderhubserver.service.TutorialService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TutorialServiceImpl implements TutorialService {

    @Autowired
    private TutorialMapper tutorialMapper;
    
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
}
