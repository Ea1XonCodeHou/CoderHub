package com.eaxon.coderhubserver.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.eaxon.coderhubcommon.constant.JwtClaimsConstant;
import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.properties.JwtProperties;
import com.eaxon.coderhubcommon.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT令牌校验拦截器 - 管理员端
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验JWT令牌
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("管理员JWT拦截器执行，URI: {}", request.getRequestURI());

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 1、从请求头中获取令牌（注意：使用 adminTokenName）
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 2、校验令牌
        try {
            log.info("管理员JWT校验开始，token: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            log.info("当前管理员ID: {}", userId);
            
            // 3、将用户ID存入ThreadLocal
            BaseContext.setCurrentId(userId);
            
            // 4、放行
            return true;
        } catch (Exception ex) {
            // 5、验证失败，响应401状态码
            log.error("管理员JWT校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}
