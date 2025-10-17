package com.example.gitlabmerge.config;

import com.example.gitlabmerge.service.MergeService;
import com.example.gitlabmerge.service.UserService;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kerwin
 * @date 2025/10/16 - 19:10
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class MergeServiceFactory {

    private static final Map<Key, MergeService> CACHE = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;

    /**
     * 基于 token 与 projectPath 获取（或创建并缓存）MergeService
     */
    private static MergeService get(String token, String projectPath) {
        Key key = new Key(token, projectPath);
        return CACHE.computeIfAbsent(key, k -> new MergeService(token, projectPath));
    }

    /**
     * 通过 user 和 projectPath 获取（内部从 Redis 读取 token）
     */
    public MergeService getByUser(String user, String projectPath) {
        if (StringUtils.isBlank(user)) {
            throw new RuntimeException("user为空");
        }
        String token = userService.getUserToken(user);
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("未找到用户token，user=" + user);
        }

        if (StringUtils.isBlank(projectPath)) {
            throw new RuntimeException("未选择项目，projectPath=" + projectPath);
        }
        return get(token, projectPath);
    }

    /**
     * 移除指定 key 的实例
     */
    public static void evict(String token, String projectPath) {
        CACHE.remove(new Key(token, projectPath));
    }

    /**
     * 清空所有缓存
     */
    public static void clear() {
        CACHE.clear();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    private static final class Key {
        private final String token;
        private final String projectPath;
    }
}
