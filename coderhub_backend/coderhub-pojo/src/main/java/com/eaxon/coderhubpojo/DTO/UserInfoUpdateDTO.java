package com.eaxon.coderhubpojo.DTO;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserInfoUpdateDTO implements Serializable {
    private String username;
    private String phone;
    private String email;
    private String avatar;
}
