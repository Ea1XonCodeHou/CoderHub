package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.TechStackDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 技术栈字典Mapper接口
 */
@Mapper
public interface TechStackDictMapper {

    /**
     * 查询所有启用的技术栈
     */
    List<TechStackDict> getAllActive();

    /**
     * 根据ID查询技术栈
     */
    TechStackDict getById(@Param("id") Long id);

    /**
     * 根据ID列表查询技术栈
     */
    List<TechStackDict> getByIds(@Param("ids") List<Long> ids);

    /**
     * 根据分类ID查询可用的技术栈
     */
    List<TechStackDict> getByCategoryId(@Param("categoryId") Long categoryId);
}

