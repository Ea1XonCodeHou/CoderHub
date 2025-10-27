package com.eaxon.coderhubserver.service;

import com.eaxon.coderhubpojo.DTO.UserAdminUpdateDTO;
import com.eaxon.coderhubpojo.DTO.UserInfoUpdateDTO;
import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserPageQueryDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.VO.UserAdminUpdateVO;
import com.eaxon.coderhubpojo.VO.UserInfoUpdateVO;
import com.eaxon.coderhubpojo.VO.UserLoginVO;
import com.eaxon.coderhubpojo.entity.User;

public interface UserService {
    User getUserById(String userId);
    void userRegister(UserRegisterDTO userRegisterDTO);
    User getUserByPhone(String userPhone);
    User getUserByEmail(String userEmail);
    UserLoginVO userLogin(UserLoginDTO userLoginDTO);
    UserInfoUpdateVO update(UserInfoUpdateDTO userInfoUpdateDTO);
    PageResult<User> pageQuery(UserPageQueryDTO userPageQueryDTO);
    UserAdminUpdateVO adminUpdateUser(UserAdminUpdateDTO userAdminUpdateDTO);
    void updateUserStatus(String userId, Integer status);
}
