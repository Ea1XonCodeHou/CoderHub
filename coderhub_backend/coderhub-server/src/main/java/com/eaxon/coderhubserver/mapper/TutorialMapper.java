package com.eaxon.coderhubserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eaxon.coderhubcommon.enumeration.OperationType;
import com.eaxon.coderhubpojo.entity.Tutorial;
import com.eaxon.coderhubserver.annotation.AutoFill;
import com.github.pagehelper.Page;

@Mapper
public interface TutorialMapper {
    
    /**
     * 插入教程
     * @param tutorial
     */
    @AutoFill(OperationType.INSERT)
    void insertTutorial(Tutorial tutorial);
    
    /**
     * 分页查询教程列表
     * @param status 状态过滤（可选）
     * @param categoryId 分类过滤（可选）
     * @return
     */
    Page<Tutorial> pageQuery(@Param("status") Integer status, @Param("categoryId") Long categoryId);
    
    /**
     * 根据ID查询教程详情
     * @param id 教程ID
     * @return 教程信息
     */
    Tutorial selectById(@Param("id") String id);
    
    /**
     * 更新教程
     * @param tutorial 教程信息
     */
    @AutoFill(OperationType.UPDATE)
    void updateTutorial(Tutorial tutorial);
    
    /**
     * 删除教程
     * @param id 教程ID
     */
    void deleteById(@Param("id") String id);
}
