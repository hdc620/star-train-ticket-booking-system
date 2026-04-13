package com.star.config;

import com.star.interceptor.JwtTokenAdminInterceptor;
import com.star.interceptor.JwtTokenUserInterceptor;
import com.star.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 扩展Spring MVC框架的消息转化器
     * @param converters
     */
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转化器加入容器中
        converters.add(converter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 1. 管理员端拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**") // 拦截所有 /admin 开头的
                .excludePathPatterns(
                        "/admin/self/login", // 只排除管理员登录
                        "/doc.html",
                        "/webjars/**",
                        "/v3/**",
                        "/swagger-resources/**",
                        "/favicon.ico"
                );

        // 2. 用户端拦截器（核心修复在这里！）
        registry.addInterceptor(jwtTokenUserInterceptor)
                // 👇 核心修复：把 /self 下需要登录的接口也加进去！
                .addPathPatterns("/user/**", "/self/view", "/self/update", "/self/password")
                // 👇 只排除用户的登录和注册
                .excludePathPatterns(
                        "/user/self/login",
                        "/user/self/register",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/**",
                        "/swagger-resources/**",
                        "/favicon.ico"
                );
    }

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("准备生成星际列车订票系统接口文档...");
        return new OpenAPI()
                .info(new Info()
                        .title("星际列车订票系统接口文档")
                        .version("1.0")
                        .description("星际列车订票系统接口文档")
                        .contact(new Contact()
                                .name("star")
                                .email("star@example.com")));
    }


}
