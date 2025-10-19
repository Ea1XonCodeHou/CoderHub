package com.eaxon.coderhubserver.service.impl;

import com.eaxon.coderhubcommon.constant.JwtClaimsConstant;
import com.eaxon.coderhubcommon.constant.MessageConstant;
import com.eaxon.coderhubcommon.constant.StatusConstant;
import com.eaxon.coderhubcommon.exception.PasswordErrorException;
import com.eaxon.coderhubcommon.exception.UserExistedException;
import com.eaxon.coderhubcommon.properties.JwtProperties;
import com.eaxon.coderhubcommon.utils.AccountGenerator;
import com.eaxon.coderhubcommon.utils.JwtUtil;
import com.eaxon.coderhubcommon.utils.MD5Util;
import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.UserLoginVO;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 用户注册
     * @param userRegisterDTO
     */
    @Override
    public void userRegister(UserRegisterDTO userRegisterDTO) {
        // 1. 检查手机号是否已注册
        String userPhone = userRegisterDTO.getPhone();
        User userByPhone = getUserByPhone(userPhone);
        if (userByPhone != null) {
            throw new UserExistedException("手机号已被注册");
        }

        // 2. 检查邮箱是否已注册
        String userEmail = userRegisterDTO.getEmail();
        User userByEmail = getUserByEmail(userEmail);
        if (userByEmail != null) {
            throw new UserExistedException("邮箱已被注册");
        }

        // 3. 生成账户编号（带重试机制，防止重复）
        String account = generateUniqueAccount();

        // 4. 构建用户对象
        User insertUser = User.builder()
                .id(UUID.randomUUID().toString())
                .account(account)
                .username(MessageConstant.DEFAULT_USERNAME_PREFIX+account)  // 用户名默认为空，用户后期自己设置
                .password(MD5Util.encrypt(userRegisterDTO.getPassword()))
                .phone(userPhone)
                .email(userEmail)
                .avatar(MessageConstant.DEFAULT_AVATAR_URL)
                .userLevel(MessageConstant.USER_DEFAULT)
                .status(StatusConstant.ENABLE)
                .role(MessageConstant.ROLE_USER)
                .build();

        // 5. 插入数据库
        userMapper.insertUser(insertUser);
    }

    /**
     * 生成唯一的账户编号（带重试机制）
     */
    private String generateUniqueAccount() {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            String account = AccountGenerator.generate();
            // 检查账户是否已存在
            User existUser = userMapper.getUserByAccount(account);
            if (existUser == null) {
                return account;
            }
        }
        throw new RuntimeException("生成账户编号失败，请重试");
    }

    @Override
    public User getUserByPhone(String userPhone) {
        return userMapper.getUserByPhone(userPhone);
    }

    @Override
    public User getUserByEmail(String userEmail) {
        return userMapper.getUserByEmail(userEmail);
    }

    @Override
    public User getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

    @Override
    public UserLoginVO userLogin(UserLoginDTO userLoginDTO) {
        String userEmail=userLoginDTO.getEmail();
        User user=userMapper.getUserByEmail(userEmail);
        if(user==null){
            throw new PasswordErrorException("邮箱出错,未找到用户");
        }
        if(!MD5Util.verify(userLoginDTO.getPassword(),user.getPassword())){
            throw new PasswordErrorException("密码错误，请重新输入");
        }
        Map<String, Object> claims=new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token= JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(),claims);
        return UserLoginVO.builder()
                .id(user.getId())
                .account(user.getAccount())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .userLevel(user.getUserLevel())
                .status(user.getStatus())
                .role(user.getRole())
                .token(token)
                .build();
    }

    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new PasswordErrorException("用户不存在");
        }

        // 2. 验证旧密码
        if (!MD5Util.verify(oldPassword, user.getPassword())) {
            throw new PasswordErrorException("原密码错误");
        }

        // 3. 更新密码
        String encryptedNewPassword = MD5Util.encrypt(newPassword);
        userMapper.updatePassword(userId, encryptedNewPassword);
    }
}
