package com.eaxon.coderhubpojo.VO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoUpdateVO implements Serializable {
    private String id;
    private String account;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private Integer userLevel;
    private Integer status;
    private String role;
}
