package com.eaxon.coderhubserver.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eaxon.coderhubcommon.constant.JwtClaimsConstant;
import com.eaxon.coderhubcommon.constant.MessageConstant;
import com.eaxon.coderhubcommon.constant.StatusConstant;
import com.eaxon.coderhubcommon.exception.PasswordErrorException;
import com.eaxon.coderhubcommon.exception.UserExistedException;
import com.eaxon.coderhubcommon.properties.JwtProperties;
import com.eaxon.coderhubcommon.utils.AccountGenerator;
import com.eaxon.coderhubcommon.utils.JwtUtil;
import com.eaxon.coderhubcommon.utils.MD5Util;
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
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.UserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public User getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

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
    public UserLoginVO userLogin(UserLoginDTO userLoginDTO) {
        String userEmail = userLoginDTO.getEmail();
        User user = userMapper.getUserByEmail(userEmail);
        if (user == null) {
            throw new PasswordErrorException("邮箱出错,未找到用户");
        }
        if (!MD5Util.verify(userLoginDTO.getPassword(), user.getPassword())) {
            throw new PasswordErrorException("密码错误，请重新输入");
        }
        
        // 根据用户角色选择不同的密钥和过期时间
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        
        String token;
        if ("ADMIN".equals(user.getRole())) {
            // 管理员使用 adminSecretKey
            token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
            log.info("管理员登录，使用 adminSecretKey 生成token");
        } else {
            // 普通用户使用 userSecretKey
            token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
            log.info("普通用户登录，使用 userSecretKey 生成token");
        }
        
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
    public UserInfoUpdateVO update(UserInfoUpdateDTO userInfoUpdateDTO) {
        // 1. 从ThreadLocal获取当前登录用户ID（JWT拦截器已设置）
        String userId = com.eaxon.coderhubcommon.context.BaseContext.getCurrentId();
        
        // 2. 查询当前用户
        User currentUser = userMapper.getUserById(userId);
        if (currentUser == null) {
            throw new UserExistedException("用户不存在");
        }
        
        // 3. 验证用户名唯一性（如果修改了）
        if (userInfoUpdateDTO.getUsername() != null 
            && !userInfoUpdateDTO.getUsername().equals(currentUser.getUsername())) {
            if (userInfoUpdateDTO.getUsername().contains(" ")) {
                throw new UserExistedException("用户名不能包含空格");
            }
            User existUser = userMapper.getUserByUsername(userInfoUpdateDTO.getUsername());
            if (existUser != null) {
                throw new UserExistedException("用户名已存在");
            }
        }
        
        // 4. 验证手机号唯一性（如果修改了）
        if (userInfoUpdateDTO.getPhone() != null 
            && !userInfoUpdateDTO.getPhone().equals(currentUser.getPhone())) {
            User existUser = userMapper.getUserByPhone(userInfoUpdateDTO.getPhone());
            if (existUser != null) {
                throw new UserExistedException("手机号已被使用");
            }
        }
        
        // 5. 验证邮箱唯一性（如果修改了）
        if (userInfoUpdateDTO.getEmail() != null 
            && !userInfoUpdateDTO.getEmail().equals(currentUser.getEmail())) {
            User existUser = userMapper.getUserByEmail(userInfoUpdateDTO.getEmail());
            if (existUser != null) {
                throw new UserExistedException("邮箱已被使用");
            }
        }
        
        // 6. 构建更新对象（只包含ID和要修改的字段，null字段不更新）
        User updateUser = User.builder()
                .id(userId)
                .username(userInfoUpdateDTO.getUsername())
                .phone(userInfoUpdateDTO.getPhone())
                .email(userInfoUpdateDTO.getEmail())
                .avatar(userInfoUpdateDTO.getAvatar())
                .build();
        
        // 7. 更新数据库（MyBatis动态SQL会自动忽略null字段）
        userMapper.update(updateUser);
        
        // 8. 查询最新用户信息
        User updatedUser = userMapper.getUserById(userId);
        
        // 9. 返回VO
        return UserInfoUpdateVO.builder()
                .id(updatedUser.getId())
                .account(updatedUser.getAccount())
                .username(updatedUser.getUsername())
                .phone(updatedUser.getPhone())
                .email(updatedUser.getEmail())
                .avatar(updatedUser.getAvatar())
                .userLevel(updatedUser.getUserLevel())
                .status(updatedUser.getStatus())
                .role(updatedUser.getRole())
                .build();
    }

    /**
     * 用户分页查询
     * @param userPageQueryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<User> pageQuery(UserPageQueryDTO userPageQueryDTO) {
        // 使用 PageHelper 进行分页
        PageHelper.startPage(userPageQueryDTO.getPage(), userPageQueryDTO.getPageSize());
        
        // 执行查询
        Page<User> page = userMapper.pageQuery(userPageQueryDTO);
        
        // 封装结果
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * 管理端修改用户信息
     * @param userAdminUpdateDTO 更新数据
     * @return 更新后的用户信息
     */
    @Override
    public UserAdminUpdateVO adminUpdateUser(UserAdminUpdateDTO userAdminUpdateDTO) {
        String userId = userAdminUpdateDTO.getUserId();
        
        // 1. 查询当前用户信息
        User currentUser = userMapper.getUserById(userId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 检查用户名是否被占用（如果修改了用户名）
        String newUsername = userAdminUpdateDTO.getUsername();
        if (newUsername != null && !newUsername.equals(currentUser.getUsername())) {
            User existUser = userMapper.getUserByUsername(newUsername);
            if (existUser != null) {
                throw new UserExistedException("用户名已被占用");
            }
        }
        
        // 3. 检查手机号是否被占用（如果修改了手机号）
        String newPhone = userAdminUpdateDTO.getPhone();
        if (newPhone != null && !newPhone.equals(currentUser.getPhone())) {
            User existUser = userMapper.getUserByPhone(newPhone);
            if (existUser != null) {
                throw new UserExistedException("手机号已被占用");
            }
        }
        
        // 4. 检查邮箱是否被占用（如果修改了邮箱）
        String newEmail = userAdminUpdateDTO.getEmail();
        if (newEmail != null && !newEmail.equals(currentUser.getEmail())) {
            User existUser = userMapper.getUserByEmail(newEmail);
            if (existUser != null) {
                throw new UserExistedException("邮箱已被占用");
            }
        }
        
        // 5. 构建更新对象
        User updateUser = User.builder()
                .id(userId)
                .username(userAdminUpdateDTO.getUsername())
                .phone(userAdminUpdateDTO.getPhone())
                .email(userAdminUpdateDTO.getEmail())
                .userLevel(userAdminUpdateDTO.getUserLevel())
                .build();
        
        // 6. 执行更新
        userMapper.update(updateUser);
        
        // 7. 查询最新用户信息
        User updatedUser = userMapper.getUserById(userId);
        
        // 8. 返回VO
        return UserAdminUpdateVO.builder()
                .id(updatedUser.getId())
                .account(updatedUser.getAccount())
                .username(updatedUser.getUsername())
                .phone(updatedUser.getPhone())
                .email(updatedUser.getEmail())
                .userLevel(updatedUser.getUserLevel())
                .status(updatedUser.getStatus())
                .build();
    }

    /**
     * 修改用户状态
     * @param userId 用户ID
     * @param status 状态：0-禁用 1-启用
     */
    @Override
    public void updateUserStatus(String userId, Integer status) {
        // 1. 检查用户是否存在
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 更新状态
        User updateUser = User.builder()
                .id(userId)
                .status(status)
                .build();
        
        userMapper.update(updateUser);
    }
}
