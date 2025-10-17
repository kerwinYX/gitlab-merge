package com.example.gitlabmerge.controller;

import com.example.gitlabmerge.config.MergeServiceFactory;
import com.example.gitlabmerge.constants.CommonConstants;
import com.example.gitlabmerge.resp.CommonResult;
import com.example.gitlabmerge.resp.GitlabProjectResp;
import com.example.gitlabmerge.resp.MergeResp;
import com.example.gitlabmerge.resp.TempBranchResp;
import com.example.gitlabmerge.service.GitLabService;
import com.example.gitlabmerge.service.MergeService;
import com.example.gitlabmerge.service.TempBranchService;
import com.example.gitlabmerge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.models.User;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author kerwin
 * @date 2025/10/16 - 14:51
 **/
@RestController
@RequestMapping("/merge")
@RequiredArgsConstructor
public class MergeController {

    private final MergeServiceFactory mergeServiceFactory;
    private final TempBranchService tempBranchService;
    private final GitLabService gitLabService;
    private final UserService userService;

    @GetMapping("/branchList")
    public CommonResult<List<String>> getBranchList(@RequestHeader("user") String user,
                                                    @RequestParam("gitPath") String gitPath) {
        return CommonResult.success(mergeServiceFactory.getByUser(user, gitPath).getBranchList());
    }

    @GetMapping("/mergeList")
    public CommonResult<List<TempBranchResp>> getMergeList(@RequestParam("gitPath") String gitPath) {
        return CommonResult.success(tempBranchService.list(gitPath));
    }
    @PostMapping("/mergeBranch")
    public CommonResult<MergeResp> mergeBranch(@RequestHeader("user") String user, @RequestBody TempBranchResp param) {
        if (StringUtils.isEmpty(param.getGitPath()) || CollectionUtils.isEmpty(param.getMergeBranchList())) {
            return CommonResult.success();
        }
        MergeService mergeService = mergeServiceFactory.getByUser(user, param.getGitPath());
        User mergeUser = mergeService.getMergeUser();
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String tmpBranch = "tmp-merge-" + System.currentTimeMillis();
        TempBranchResp tempBranchResp = new TempBranchResp()
                .setBranchName(tmpBranch)
                .setMergeUser(mergeUser.getUsername())
                .setMergeTime(startTime)
                .setGitPath(param.getGitPath())
                .setMergeBranchList(param.getMergeBranchList())
                .setMergeStatus(CommonConstants.MERGE_STATUS_RUNNING);
        tempBranchService.upsertByBranchName(param.getGitPath(), tempBranchResp);
        MergeResp mergeResp = new MergeResp();
        mergeResp.setTempBranch(tmpBranch).setMergeUser(mergeUser.getUsername());
        try {
            mergeService.mergeBranch(tmpBranch, param.getMergeBranchList());
            tempBranchService.updateStatusByBranchName(param.getGitPath(), tmpBranch, CommonConstants.MERGE_STATUS_SUCCESS);
            return CommonResult.success(mergeResp);
        } catch (Exception ex) {
            mergeResp.setMergeStatus(CommonConstants.MERGE_STATUS_FAIL).setMergeMsg(ex.getMessage());
            tempBranchService.updateStatusByBranchName(param.getGitPath(), tmpBranch, CommonConstants.MERGE_STATUS_FAIL);
            return CommonResult.success(mergeResp);
        }
    }

    @PostMapping("/retryMergeBranch")
    public CommonResult<MergeResp> retryMergeBranch(@RequestHeader("user") String user, @RequestBody TempBranchResp param) {
        String branchName = param.getBranchName();
        String gitPath = param.getGitPath();
        MergeResp mergeResp = new MergeResp();
        mergeResp.setTempBranch(branchName);
        if (StringUtils.isEmpty(branchName) || StringUtils.isBlank(gitPath)) {
            mergeResp.setMergeMsg("参数异常");
            return CommonResult.success(mergeResp);
        }
        TempBranchResp retryBranch = tempBranchService.getByBranchName(gitPath, branchName);

        if (Objects.isNull(retryBranch)) {
            mergeResp.setMergeMsg("分支不存在");
            return CommonResult.success(mergeResp);
        }
        MergeService mergeService = mergeServiceFactory.getByUser(user, gitPath);
        mergeResp.setMergeUser(mergeService.getMergeUser().getUsername());

        try {
            mergeService.mergeBranch(branchName, retryBranch.getMergeBranchList());
            tempBranchService.updateStatusByBranchName(gitPath, branchName, CommonConstants.MERGE_STATUS_SUCCESS);
            return CommonResult.success(mergeResp);
        } catch (Exception ex) {
            mergeResp.setMergeStatus(CommonConstants.MERGE_STATUS_FAIL).setMergeMsg(ex.getMessage());
            tempBranchService.updateStatusByBranchName(gitPath, branchName, CommonConstants.MERGE_STATUS_FAIL);
            return CommonResult.success(mergeResp);
        }
    }



    @GetMapping("/getGitlabProject")
    public CommonResult<List<GitlabProjectResp>> getGitlabProject() {
        return CommonResult.success(gitLabService.getGitlabProject());
    }

    @GetMapping("/initUserToken")
    public CommonResult<Boolean> initUserToken(@RequestParam("user") String user, @RequestParam("token") String token) {
        userService.setUserToken(user, token);
        return CommonResult.success(true);
    }

    @GetMapping("/iniGitlabProject")
    public CommonResult<Boolean> iniGitlabProject(@RequestParam("projectName") String projectName, @RequestParam("projectPath") String projectPath) {
        gitLabService.initGitlabProject(projectName, projectPath);
        return CommonResult.success(true);
    }

}
