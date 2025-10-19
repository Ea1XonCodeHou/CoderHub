package com.eaxon.coderhubserver.controller;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.UserLoginVO;
import com.eaxon.coderhubserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<String> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：phone={}, email={}", userRegisterDTO.getPhone(), userRegisterDTO.getEmail());
        
        userService.userRegister(userRegisterDTO);
        
        log.info("用户注册成功");
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录: {}",userLoginDTO);
        UserLoginVO userLoginVO=userService.userLogin(userLoginDTO);
        log.info("用户登录成功");
        return Result.success(userLoginVO);
    }

    /**
     * 修改密码（测试JWT拦截器）
     * 需要登录才能访问，会被JWT拦截器拦截
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<String> updatePassword(@RequestBody Map<String, String> passwordMap) {
        // 从ThreadLocal中获取当前用户ID
        String userId = BaseContext.getCurrentId();
        log.info("修改密码，userId: {}", userId);
        
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        // 调用Service层修改密码
        userService.updatePassword(userId, oldPassword, newPassword);
        
        log.info("密码修改成功");
        return Result.success("密码修改成功");
    }
}
