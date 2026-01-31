package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubpojo.VO.ProjectVO;
import com.eaxon.coderhubpojo.entity.ProjectCategory;
import com.eaxon.coderhubpojo.entity.TechStackDict;
import com.eaxon.coderhubserver.mapper.ProjectCategoryMapper;
import com.eaxon.coderhubserver.mapper.TechStackDictMapper;
import com.eaxon.coderhubserver.service.ProjectCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目缓存服务实现类
 * 分类和技术栈数据首次从数据库加载后缓存到Redis，不设置过期时间
 */
@Service
@Slf4j
public class ProjectCacheServiceImpl implements ProjectCacheService {

    private static final String CACHE_KEY_CATEGORIES = "project:categories";
    private static final String CACHE_KEY_TECH_STACKS = "project:tech_stacks";
    private static final String CACHE_KEY_CATEGORY_TECH_MAP = "project:category_tech_map";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProjectCategoryMapper categoryMapper;

    @Autowired
    private TechStackDictMapper techStackMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ProjectVO.CategoryItem> getAllCategories() {
        try {
            // 1. 先从Redis读取
            String cached = stringRedisTemplate.opsForValue().get(CACHE_KEY_CATEGORIES);
            if (cached != null) {
                log.debug("从Redis缓存读取项目分类数据");
                return objectMapper.readValue(cached, new TypeReference<List<ProjectVO.CategoryItem>>() {});
            }

            // 2. 缓存不存在，从数据库加载
            log.info("首次加载项目分类数据，从数据库读取并写入Redis缓存");
            List<ProjectVO.CategoryItem> categories = loadCategoriesFromDB();

            // 3. 序列化为JSON并写入Redis（不设置过期时间）
            String jsonValue = objectMapper.writeValueAsString(categories);
            stringRedisTemplate.opsForValue().set(CACHE_KEY_CATEGORIES, jsonValue);
            log.info("项目分类数据已缓存到Redis，共{}个分类", categories.size());

            return categories;
        } catch (Exception e) {
            log.error("获取项目分类缓存失败，从数据库读取", e);
            return loadCategoriesFromDB();
        }
    }

    @Override
    public List<ProjectVO.TechStackItem> getTechStacksByCategoryId(Long categoryId) {
        try {
            // 1. 先获取分类-技术栈映射缓存
            String cached = (String) stringRedisTemplate.opsForHash().get(CACHE_KEY_CATEGORY_TECH_MAP, categoryId.toString());
            if (cached != null) {
                log.debug("从Redis缓存读取分类{}的技术栈数据", categoryId);
                return objectMapper.readValue(cached, new TypeReference<List<ProjectVO.TechStackItem>>() {});
            }

            // 2. 缓存不存在，从数据库加载
            log.info("首次加载分类{}的技术栈数据", categoryId);
            List<TechStackDict> techStacks = techStackMapper.getByCategoryId(categoryId);
            List<ProjectVO.TechStackItem> items = techStacks.stream()
                    .map(ts -> ProjectVO.TechStackItem.builder()
                            .id(ts.getId())
                            .techName(ts.getTechName())
                            .build())
                    .collect(Collectors.toList());

            // 3. 序列化为JSON并写入Redis Hash
            String jsonValue = objectMapper.writeValueAsString(items);
            stringRedisTemplate.opsForHash().put(CACHE_KEY_CATEGORY_TECH_MAP, categoryId.toString(), jsonValue);
            log.info("分类{}的技术栈数据已缓存到Redis，共{}个技术栈", categoryId, items.size());

            return items;
        } catch (Exception e) {
            log.error("获取分类{}技术栈缓存失败，从数据库读取", categoryId, e);
            List<TechStackDict> techStacks = techStackMapper.getByCategoryId(categoryId);
            return techStacks.stream()
                    .map(ts -> ProjectVO.TechStackItem.builder()
                            .id(ts.getId())
                            .techName(ts.getTechName())
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void refreshCache() {
        try {
            log.info("开始刷新项目分类和技术栈缓存");
            
            // 删除旧缓存
            stringRedisTemplate.delete(CACHE_KEY_CATEGORIES);
            stringRedisTemplate.delete(CACHE_KEY_TECH_STACKS);
            stringRedisTemplate.delete(CACHE_KEY_CATEGORY_TECH_MAP);

            // 重新加载
            getAllCategories();
            
            log.info("项目分类和技术栈缓存刷新完成");
        } catch (Exception e) {
            log.error("刷新缓存失败", e);
        }
    }

    @Override
    public boolean isCacheExists() {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(CACHE_KEY_CATEGORIES));
        } catch (Exception e) {
            log.error("检查缓存是否存在失败", e);
            return false;
        }
    }

    /**
     * 从数据库加载分类数据（带技术栈）
     */
    private List<ProjectVO.CategoryItem> loadCategoriesFromDB() {
        List<ProjectCategory> categories = categoryMapper.getAllActive();
        List<ProjectVO.CategoryItem> result = new ArrayList<>();

        for (ProjectCategory category : categories) {
            // 查询该分类下的技术栈
            List<TechStackDict> techStacks = techStackMapper.getByCategoryId(category.getId());
            List<ProjectVO.TechStackItem> techItems = techStacks.stream()
                    .map(ts -> ProjectVO.TechStackItem.builder()
                            .id(ts.getId())
                            .techName(ts.getTechName())
                            .build())
                    .collect(Collectors.toList());

            ProjectVO.CategoryItem item = ProjectVO.CategoryItem.builder()
                    .id(category.getId())
                    .categoryName(category.getCategoryName())
                    .techStacks(techItems)
                    .build();

            result.add(item);

            // 同时缓存每个分类的技术栈映射（序列化为JSON）
            try {
                String jsonValue = objectMapper.writeValueAsString(techItems);
                stringRedisTemplate.opsForHash().put(CACHE_KEY_CATEGORY_TECH_MAP, category.getId().toString(), jsonValue);
            } catch (Exception e) {
                log.warn("缓存分类{}技术栈映射失败", category.getId(), e);
            }
        }

        return result;
    }
}

