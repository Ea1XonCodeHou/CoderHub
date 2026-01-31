package com.eaxon.coderhubpojo.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    private String phone;
    private String email;
    private String password;
}
