package com.eaxon.coderhubpojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理端修改用户信息DTO
 */
@Data
@Schema(description = "管理端修改用户信息请求参数")
public class UserAdminUpdateDTO implements Serializable {
    private String userId;
    private String username;
    private String phone;
    private String email;
    private Integer userLevel;
}
