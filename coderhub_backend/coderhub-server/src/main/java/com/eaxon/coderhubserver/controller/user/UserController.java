package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.UserInfoUpdateDTO;
import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.UserInfoUpdateVO;
import com.eaxon.coderhubpojo.VO.UserLoginVO;
import com.eaxon.coderhubpojo.VO.UserStatsVO;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.service.UserFollowService;
import com.eaxon.coderhubserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserFollowService userFollowService;
    
    @Autowired
    private ArticleMapper articleMapper;
    
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

    /**
     * 获取用户统计信息（文章数、关注数、粉丝数）
     */
    @GetMapping("/{userId}/stats")
    @ApiOperation("获取用户统计信息")
    public Result<UserStatsVO> getUserStats(@PathVariable String userId) {
        log.info("获取用户统计信息：userId={}", userId);
        
        // 统计文章数
        Integer articleCount = articleMapper.countByUserId(userId);
        
        // 统计关注数
        Integer followingCount = userFollowService.getFollowingCount(userId);
        
        // 统计粉丝数
        Integer followersCount = userFollowService.getFollowersCount(userId);
        
        // 检查当前用户是否已关注该用户
        Boolean isFollowing = false;
        String currentUserId = BaseContext.getCurrentId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowing = userFollowService.isFollowing(currentUserId, userId);
        }
        
        UserStatsVO statsVO = UserStatsVO.builder()
                .articleCount(articleCount != null ? articleCount : 0)
                .followingCount(followingCount)
                .followersCount(followersCount)
                .isFollowing(isFollowing)
                .build();
        
        return Result.success(statsVO);
    }

    /**
     * 关注/取消关注用户
     */
    @PostMapping("/{userId}/follow")
    @ApiOperation("关注/取消关注用户")
    public Result<Map<String, Object>> toggleFollow(@PathVariable String userId) {
        String currentUserId = BaseContext.getCurrentId();
        log.info("用户{}关注/取消关注用户{}", currentUserId, userId);
        
        Boolean isFollowing = userFollowService.toggleFollow(currentUserId, userId);
        
        // 重新查询粉丝数
        Integer followersCount = userFollowService.getFollowersCount(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        result.put("followersCount", followersCount);
        
        return Result.success(result);
    }

    /**
     * 检查是否已关注某用户
     */
    @GetMapping("/{userId}/follow/status")
    @ApiOperation("检查是否已关注某用户")
    public Result<Boolean> checkFollowStatus(@PathVariable String userId) {
        String currentUserId = BaseContext.getCurrentId();
        log.info("检查用户{}是否关注用户{}", currentUserId, userId);
        
        Boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);
        
        return Result.success(isFollowing);
    }
}
