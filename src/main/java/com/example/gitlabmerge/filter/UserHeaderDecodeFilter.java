package com.example.gitlabmerge.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author kerwin
 * @date 2025/10/17 - 19:31
 **/
@Component
public class UserHeaderDecodeFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getHeader("user") != null) {
            // 包装请求
            UserHeaderRequestWrapper wrappedRequest = new UserHeaderRequestWrapper(httpRequest);
            // 传入包装后的 request
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
