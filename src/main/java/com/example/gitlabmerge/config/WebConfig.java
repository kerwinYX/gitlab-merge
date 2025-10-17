package com.example.gitlabmerge.config;

import com.example.gitlabmerge.interceptor.UserHeaderDecodeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author kerwin
 * @date 2025/10/17 - 19:22
 **/
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private UserHeaderDecodeInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**");
    }
}
