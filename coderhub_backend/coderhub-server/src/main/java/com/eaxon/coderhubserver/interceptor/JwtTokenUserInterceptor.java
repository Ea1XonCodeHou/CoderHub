package com.eaxon.coderhubserver.interceptor;

import com.eaxon.coderhubcommon.constant.JwtClaimsConstant;
import com.eaxon.coderhubcommon.context.BaseContext;
import com.eaxon.coderhubcommon.properties.JwtProperties;
import com.eaxon.coderhubcommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验拦截器 - 用户端
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

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
        log.info("JWT拦截器执行，URI: {}", request.getRequestURI());

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2、校验令牌
        try {
            log.info("JWT校验开始，token: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
            log.info("当前用户ID: {}", userId);
            
            // 3、将用户ID存入ThreadLocal
            BaseContext.setCurrentId(userId);
            
            // 4、放行
            return true;
        } catch (Exception ex) {
            // 5、验证失败，响应401状态码
            log.error("JWT校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}
