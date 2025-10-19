package com.eaxon.coderhubserver.mapper;

import com.eaxon.coderhubpojo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    /**
     * 根据电话查询用户
     * @param userPhone 手机号
     * @return 用户信息
     */
    @Select("select * from coder_hub.user where phone = #{userPhone}")
    User getUserByPhone(String userPhone);

    /**
     * 根据邮箱查询用户
     * @param userEmail 邮箱
     * @return 用户信息
     */
    @Select("select * from coder_hub.user where email = #{userEmail}")
    User getUserByEmail(String userEmail);

    /**
     * 根据账户编号查询用户
     * @param account 账户编号
     * @return 用户信息
     */
    @Select("select * from coder_hub.user where account = #{account}")
    User getUserByAccount(String account);

    /**
     * 根据用户ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    @Select("select * from coder_hub.user where id = #{userId}")
    User getUserById(String userId);

    /**
     * 插入新用户
     * @param user 用户信息
     */
    @Insert("insert into coder_hub.user(id, account, username, password, phone, email, avatar, user_level, status, role) " +
            "values (#{id}, #{account}, #{username}, #{password}, #{phone}, #{email}, #{avatar}, #{userLevel}, #{status}, #{role})")
    void insertUser(User user);

    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param password 新密码（已加密）
     */
    @Update("update coder_hub.user set password = #{password} where id = #{userId}")
    void updatePassword(String userId, String password);
}
