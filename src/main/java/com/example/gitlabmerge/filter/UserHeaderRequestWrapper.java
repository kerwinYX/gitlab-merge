package com.example.gitlabmerge.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author kerwin
 * @date 2025/10/17 - 19:24
 **/
@Slf4j
public class UserHeaderRequestWrapper extends HttpServletRequestWrapper {

    private final String decodedUser;

    public UserHeaderRequestWrapper(HttpServletRequest request) {
        super(request);
        String userHeader = request.getHeader("user");
        if (userHeader != null) {
            String tmp;
            try {
                tmp = URLDecoder.decode(userHeader, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                tmp = userHeader;
            }
            decodedUser = tmp;
        } else {
            decodedUser = null;
        }
    }

    @Override
    public String getHeader(String name) {
        if ("user".equalsIgnoreCase(name)) {
            return decodedUser;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("user".equalsIgnoreCase(name)) {
            if (decodedUser != null) {
                return Collections.enumeration(Collections.singleton(decodedUser));
            } else {
                return Collections.emptyEnumeration();
            }
        }
        return super.getHeaders(name);
    }
}
