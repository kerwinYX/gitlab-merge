package com.example.gitlabmerge.interceptor;

import com.example.gitlabmerge.filter.UserHeaderRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kerwin
 * @date 2025/10/17 - 19:20
 **/
@Component
public class UserHeaderDecodeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        // 包装请求，替换 header
//        if (request.getHeader("user") != null) {
//            UserHeaderRequestWrapper wrappedRequest = new UserHeaderRequestWrapper(request);
//            request.setAttribute("WRAPPED_REQUEST", wrappedRequest);
//        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // 不需要操作
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 不需要操作
    }
}
