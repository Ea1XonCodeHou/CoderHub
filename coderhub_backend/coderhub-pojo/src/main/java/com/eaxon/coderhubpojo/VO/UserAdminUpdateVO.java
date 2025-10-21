package com.eaxon.coderhubpojo.VO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理端修改用户信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "管理端修改用户信息返回数据")
public class UserAdminUpdateVO implements Serializable {
    private String id;
    private String account;
    private String username;
    private String phone;
    private String email;
    private Integer userLevel;
    private Integer status;
}

