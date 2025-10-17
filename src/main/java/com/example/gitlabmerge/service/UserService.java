package com.example.gitlabmerge.service;

import com.example.gitlabmerge.constants.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @author kerwin
 * @date 2025/10/16 - 21:36
 **/
@Service
public class UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public String getUserToken(String user) {
        return stringRedisTemplate.opsForValue().get(RedisConstants.TOKEN_KEY_PREFIX + user);
    }

    public void setUserToken(String user, String token) {
        stringRedisTemplate.opsForValue().set(RedisConstants.TOKEN_KEY_PREFIX + user, token);
    }
}
