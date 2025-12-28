package com.eaxon.coderhubserver.controller.admin;

import com.eaxon.coderhubcommon.result.Result;
import com.eaxon.coderhubpojo.DTO.UserAdminUpdateDTO;
import com.eaxon.coderhubpojo.DTO.UserPageQueryDTO;
import com.eaxon.coderhubpojo.VO.PageResult;
import com.eaxon.coderhubpojo.VO.UserAdminUpdateVO;
import com.eaxon.coderhubpojo.entity.User;
import com.eaxon.coderhubserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@Slf4j
@Tag(name = "管理端-用户管理接口")
public class UserManagerController {
    
    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     * @param userPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    public Result<PageResult<User>> page(UserPageQueryDTO userPageQueryDTO) {
        log.info("分页查询用户列表：{}", userPageQueryDTO);
        PageResult<User> pageResult = userService.pageQuery(userPageQueryDTO);
        log.info("查询结果：总记录数={}, 当前页记录数={}", pageResult.getTotal(), pageResult.getRecords().size());
        return Result.success(pageResult);
    }

    /**
     * 编辑用户信息
     * @param userAdminUpdateDTO 更新数据
     * @return 更新后的用户信息
     */
    @PutMapping("/update")
    @Operation(summary = "编辑用户信息")
    public Result<UserAdminUpdateVO> updateUser(@RequestBody UserAdminUpdateDTO userAdminUpdateDTO) {
        log.info("管理端编辑用户信息：{}", userAdminUpdateDTO);
        UserAdminUpdateVO result = userService.adminUpdateUser(userAdminUpdateDTO);
        log.info("用户信息编辑成功：{}", result);
        return Result.success(result);
    }

    /**
     * 修改用户状态（启用/禁用）
     * @param userId 用户ID
     * @param status 状态：0-禁用 1-启用
     * @return 操作结果
     */
    @PutMapping("/status/{userId}/{status}")
    @Operation(summary = "修改用户状态")
    public Result<String> updateStatus(@PathVariable String userId, @PathVariable Integer status) {
        log.info("修改用户状态：userId={}, status={}", userId, status);
        userService.updateUserStatus(userId, status);
        String message = status == 1 ? "用户已启用" : "用户已禁用";
        log.info(message);
        return Result.success(message);
    }
}
