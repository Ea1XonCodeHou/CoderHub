package com.eaxon.coderhubserver.controller.user;

import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.UserInfoUpdateDTO;
import com.eaxon.coderhubpojo.DTO.UserLoginDTO;
import com.eaxon.coderhubpojo.DTO.UserRegisterDTO;
import com.eaxon.coderhubpojo.VO.*;
import com.eaxon.coderhubpojo.entity.Article;
import com.eaxon.coderhubpojo.entity.Project;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.mapper.ArticleMapper;
import com.eaxon.coderhubserver.mapper.ProjectMapper;
import com.eaxon.coderhubserver.mapper.UserFollowMapper;
import com.eaxon.coderhubserver.mapper.UserMapper;
import com.eaxon.coderhubserver.service.UserFollowService;
import com.eaxon.coderhubserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserFollowService userFollowService;
    
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;
    
    /**
     * 用户注册
     * @param userRegisterDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
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
    @Operation(summary = "用户登录")
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
    @Operation(summary = "修改用户信息")
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
    @Operation(summary = "获取用户信息")
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
    @Operation(summary = "获取用户统计信息")
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
    @Operation(summary = "关注/取消关注用户")
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
    @Operation(summary = "检查是否已关注某用户")
    public Result<Boolean> checkFollowStatus(@PathVariable String userId) {
        String currentUserId = BaseContext.getCurrentId();
        log.info("检查用户{}是否关注用户{}", currentUserId, userId);
        
        Boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);
        
        return Result.success(isFollowing);
    }

    // ==================== 个人中心相关接口 ====================

    /**
     * 获取我的文章列表
     */
    @GetMapping("/profile/articles")
    @Operation(summary = "获取我的文章列表")
    public Result<List<UserArticleVO>> getMyArticles() {
        String userId = BaseContext.getCurrentId();
        log.info("获取用户{}的文章列表", userId);

        List<Article> articles = articleMapper.getByUserId(userId);
        List<UserArticleVO> result = new ArrayList<>();
        
        for (Article article : articles) {
            UserArticleVO vo = UserArticleVO.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .summary(article.getSummary())
                    .coverImage(article.getCoverImage())
                    .viewCount(Math.toIntExact(article.getViewCount()))
                    .likeCount(article.getLikeCount())
                    .commentCount(article.getCommentCount())
                    .createTime(article.getCreateTime())
                    .build();
            result.add(vo);
        }

        return Result.success(result);
    }

    /**
     * 获取我关注的用户列表
     */
    @GetMapping("/profile/following")
    @Operation(summary = "获取我关注的用户列表")
    public Result<List<UserFollowVO>> getMyFollowing() {
        String currentUserId = BaseContext.getCurrentId();
        log.info("获取用户{}的关注列表", currentUserId);

        List<String> followedIds = userFollowMapper.getFollowedIdsByUserId(currentUserId);
        List<UserFollowVO> result = new ArrayList<>();

        for (String followedId : followedIds) {
            User user = userMapper.getUserById(followedId);
            if (user != null) {
                Integer articleCount = articleMapper.countByUserId(followedId);
                Integer followersCount = userFollowService.getFollowersCount(followedId);

                UserFollowVO vo = UserFollowVO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .avatar(user.getAvatar())
                        .articleCount(articleCount != null ? articleCount : 0)
                        .followersCount(followersCount)
                        .isFollowing(true)  // 在我的关注列表中，肯定是已关注
                        .build();
                result.add(vo);
            }
        }

        return Result.success(result);
    }

    /**
     * 获取我的粉丝列表
     */
    @GetMapping("/profile/followers")
    @Operation(summary = "获取我的粉丝列表")
    public Result<List<UserFollowVO>> getMyFollowers() {
        String currentUserId = BaseContext.getCurrentId();
        log.info("获取用户{}的粉丝列表", currentUserId);

        List<String> followerIds = userFollowMapper.getFollowerIdsByUserId(currentUserId);
        List<UserFollowVO> result = new ArrayList<>();

        for (String followerId : followerIds) {
            User user = userMapper.getUserById(followerId);
            if (user != null) {
                Integer articleCount = articleMapper.countByUserId(followerId);
                Integer followersCount = userFollowService.getFollowersCount(followerId);
                Boolean isFollowing = userFollowService.isFollowing(currentUserId, followerId);

                UserFollowVO vo = UserFollowVO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .avatar(user.getAvatar())
                        .articleCount(articleCount != null ? articleCount : 0)
                        .followersCount(followersCount)
                        .isFollowing(isFollowing)  // 检查是否互相关注
                        .build();
                result.add(vo);
            }
        }

        return Result.success(result);
    }

    /**
     * 获取我的项目列表
     */
    @GetMapping("/profile/projects")
    @Operation(summary = "获取我的项目列表")
    public Result<List<UserProjectVO>> getMyProjects() {
        String userId = BaseContext.getCurrentId();
        log.info("获取用户{}的项目列表", userId);

        // 获取用户所有有效状态的项目
        List<Project> projects = projectMapper.getByUserId(userId, null, 1);
        List<UserProjectVO> result = new ArrayList<>();

        for (Project project : projects) {
            UserProjectVO vo = UserProjectVO.builder()
                    .id(project.getId())
                    .projectName(project.getProjectName())
                    .shortDescription(project.getShortDescription())
                    .coverImage(project.getCoverImage())
                    .gitUrl(project.getGitUrl())
                    .demoUrl(project.getDemoUrl())
                    .viewCount(project.getViewCount())
                    .isOpenSource(project.getIsOpenSource())
                    .createTime(project.getCreatedAt())
                    .build();
            result.add(vo);
        }

        return Result.success(result);
    }

    /**
     * 修改密码
     */
    @PostMapping("/profile/password")
    @Operation(summary = "修改密码")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        String userId = BaseContext.getCurrentId();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        log.info("用户{}修改密码", userId);
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("旧密码和新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        
        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
