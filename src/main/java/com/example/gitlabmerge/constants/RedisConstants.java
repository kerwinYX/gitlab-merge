package com.example.gitlabmerge.constants;

/**
 * @author kerwin
 * @date 2025/10/16 - 15:20
 **/
public class RedisConstants {
    public static final String TOKEN_KEY_PREFIX = "GITLAB_TOKEN:";
    public static final String TEMP_BRANCH_KEY_PREFIX = "TEMP_BRANCH:";
    public static final String  GITLAB_PROJECT_KEY_PREFIX = "GITLAB_PROJECT";
    // 每个 key 最多保留 10 条记录
    public static final int TEMP_BRANCH_MAX_PER_KEY = 10;

}
