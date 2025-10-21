package com.eaxon.coderhubpojo.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户分页查询DTO
 */
@Data
@ApiModel(description = "用户分页查询请求参数")
public class UserPageQueryDTO implements Serializable {
    private Integer page = 1;
    private Integer pageSize = 10;

    private String username;
    private String phone;
    private String email;
    private String account;
    private Integer userLevel;
    private Integer status;
}

