package com.example.gitlabmerge.config;

import org.gitlab4j.api.GitLabApi;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kerwin
 * @date 2025/10/9 - 16:01
 **/
public class GitLabApiFactory {

    private static final ConcurrentHashMap<String, GitLabApi> CACHE = new ConcurrentHashMap<>();

    public static GitLabApi get(String baseUrl, String token) {
        String key = baseUrl + "|" + token;
        return CACHE.computeIfAbsent(key, k -> new GitLabApi(baseUrl, token));
    }

    public static void remove(String baseUrl, String token) {
        String key = baseUrl + "|" + token;
        GitLabApi gitLabApi = CACHE.get(key);
        if (Objects.isNull(gitLabApi)) {
            return;
        }
        gitLabApi.close();
        CACHE.remove(key);
    }

    public static void clear() {
        for (GitLabApi api : CACHE.values()) {
            api.close();
        }
        CACHE.clear();
    }
}
