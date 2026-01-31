package com.eaxon.coderhubpojo.DTO;

import lombok.Data;
import java.io.Serializable;

/**
 * 分类操作DTO（用于新增和编辑）
 */
@Data
public class CategoryDTO implements Serializable {
    private String id;
    private String categoryName;
    private String parentId;
    private Integer sortOrder;
    private String description;
    private String icon;
    private Integer status;
}

