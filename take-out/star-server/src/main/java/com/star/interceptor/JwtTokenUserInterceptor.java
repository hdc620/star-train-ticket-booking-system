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

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("========== 1. 拦截到请求，URI: {} ==========", requestURI);

        // 1. 放行文档相关
        if (requestURI.contains("doc.html")
                || requestURI.contains("webjars")
                || requestURI.contains("v3")
                || requestURI.contains("swagger-resources")
                || requestURI.contains("favicon.ico")) {
            log.info("========== 2. 文档相关请求，直接放行 ==========");
            return true;
        }

        // 2. 放行静态资源
        if (!(handler instanceof HandlerMethod)) {
            log.info("========== 2. 静态资源请求，直接放行 ==========");
            return true;
        }

        // 3. 打印配置的 Header 名称，确认是不是 "authentication"
        String headerName = jwtProperties.getUserTokenName();
        log.info("========== 3. 配置的 Token Header 名称是: {} ==========", headerName);

        // 4. 从请求头获取 Token
        String token = request.getHeader(headerName);
        log.info("========== 4. 从请求头拿到的 Token 是: {} ==========", token);

        // 5. 如果 Token 为空，直接返回 401
        if (token == null || token.trim().isEmpty()) {
            log.error("========== 5. Token 为空，拒绝访问 ==========");
            response.setStatus(401);
            return false;
        }

        // 6. 校验 Token
        try {
            log.info("========== 5. 开始解析 Token... ==========");
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("========== 6. 解析成功，用户 ID: {} ==========", userId); // ✅ 这里加上了 {}

            // 7. 把 ID 放到 ThreadLocal 里
            BaseContext.setCurrentId(userId);
            log.info("========== 7. 已将用户 ID 放入 BaseContext，放行 ==========");

            return true;
        } catch (Exception ex) {
            log.error("========== 6. Token 解析失败！错误信息: {} ==========", ex.getMessage());
            ex.printStackTrace(); // 打印完整堆栈
            response.setStatus(401);
            return false;
        }
    }
}
