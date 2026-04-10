package com.star.interceptor;

import com.star.constant.JwtClaimsConstant;
import com.star.context.BaseContext;
import com.star.properties.JwtProperties;
import com.star.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 👇 【核心修复】在最开头判断：如果是 Knife4j/SpringDoc 相关请求，直接放行，不做任何校验
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);

        // 只要是 Knife4j/Swagger/SpringDoc 相关的，直接通过
        if (requestURI.contains("doc.html")
                || requestURI.contains("webjars")
                || requestURI.contains("v3")
                || requestURI.contains("swagger-resources")
                || requestURI.contains("favicon.ico")) {
            return true;
        }

        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            log.info("当前管理员id：", adminId);
            BaseContext.setCurrentId(adminId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
