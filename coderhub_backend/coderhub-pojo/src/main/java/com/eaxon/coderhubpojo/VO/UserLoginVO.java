package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {
    private String id;
    private String account;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private Integer userLevel;
    private Integer status;
    private String role;
    
    private String token;
}
