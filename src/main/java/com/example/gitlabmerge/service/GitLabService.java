package com.example.gitlabmerge.service;

import com.example.gitlabmerge.constants.RedisConstants;
import com.example.gitlabmerge.resp.GitlabProjectResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author kerwin
 * @date 2025/10/16 - 21:22
 **/
@Service
public class GitLabService {

    @Resource
    private RedisTemplate<String, GitlabProjectResp> redisTemplate;

    public List<GitlabProjectResp> getGitlabProject(){
        return redisTemplate.opsForList().range(RedisConstants.GITLAB_PROJECT_KEY_PREFIX, 0, -1);
    }

    public void initGitlabProject(String projectName, String projectPath) {
        if(StringUtils.isBlank(projectName) || StringUtils.isBlank(projectPath)){
            return;
        }
        GitlabProjectResp gitlabProjectResp = new GitlabProjectResp();
        gitlabProjectResp.setProjectName(projectName);
        gitlabProjectResp.setProjectPath(projectPath);
        redisTemplate.opsForList().leftPush(RedisConstants.GITLAB_PROJECT_KEY_PREFIX, gitlabProjectResp);
    }
}
