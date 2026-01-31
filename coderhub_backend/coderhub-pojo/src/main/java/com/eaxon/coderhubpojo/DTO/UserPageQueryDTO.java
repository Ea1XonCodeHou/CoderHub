package com.eaxon.coderhubpojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户分页查询DTO
 */
@Data
@Schema(description = "用户分页查询参数")
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
