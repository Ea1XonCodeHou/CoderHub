package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.UserLoginVO;
import com.eaxon.coderhubpojo.entity.User;

public interface UserService {
    void userRegister(UserRegisterDTO userRegisterDTO);
    User getUserByPhone(String userPhone);
    User getUserByEmail(String userEmail);
    User getUserById(String userId);
    UserLoginVO userLogin(UserLoginDTO userLoginDTO);
    void updatePassword(String userId, String oldPassword, String newPassword);
}
