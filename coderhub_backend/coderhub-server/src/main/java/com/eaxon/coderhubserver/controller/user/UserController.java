package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.UserInfoUpdateDTO;
import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.UserInfoUpdateVO;
import com.eaxon.coderhubpojo.VO.UserLoginVO;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
     * 修改用户信息
     */
    @PutMapping("/updateInfo")
    @ApiOperation("修改用户信息")
    public Result<UserInfoUpdateVO> userUpdateInfo(@RequestBody UserInfoUpdateDTO userInfoUpdateDTO){
        log.info("修改用户信息：{}", userInfoUpdateDTO);
        UserInfoUpdateVO userInfoUpdateVO = userService.update(userInfoUpdateDTO);
        log.info("用户信息修改成功");
        return Result.success(userInfoUpdateVO);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{userId}")
    @ApiOperation("获取用户信息")
    public Result<UserLoginVO> getUserById(@PathVariable String userId) {
        log.info("获取用户信息：userId={}", userId);
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserLoginVO vo = new UserLoginVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }
}
