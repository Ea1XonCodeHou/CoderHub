package com.eaxon.coderhubpojo.VO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果通用VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分页查询结果")
public class PageResult<T> implements Serializable {

    private Long total;
    private List<T> records;
}

