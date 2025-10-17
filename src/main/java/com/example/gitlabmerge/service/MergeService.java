package com.example.gitlabmerge.service;

import com.example.gitlabmerge.config.GitLabApiFactory;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

/**
 * @author kerwin
 * @date 2025/10/16 - 14:39
 **/
@Slf4j
public class MergeService {
    private static final String BASE_URL = "https://gitlab.hypergryph.net";
    private static final String BASE_BRANCH = "master";
    private static final String BRANCH_MAIN = "main";
    private final String token;
    private final String projectPath;

    public MergeService(String token, String projectPath) {
        this.token = token;
        this.projectPath = projectPath;
    }

    public User getMergeUser() {
        GitLabApi gitLabApi = GitLabApiFactory.get(BASE_URL, token);
        try {
            return gitLabApi.getUserApi().getCurrentUser();
        } catch (Exception e){
            return new User();
        }
    }

    /** 获取项目分支列表，排除 main/master */
    public List<Branch> getBranches(Object projectIdOrPath) throws Exception {
        GitLabApi gitLabApi = GitLabApiFactory.get(BASE_URL, token);
        return gitLabApi.getRepositoryApi().getBranches(projectIdOrPath);
    }

    public List<String> filterBranches(List<Branch> branches) {
        return branches.stream()
                .sorted((b1, b2) -> b2.getCommit().getCommittedDate().compareTo(b1.getCommit().getCommittedDate()))
                .map(Branch::getName)
                .filter(name -> !name.equals(BRANCH_MAIN) && !name.equals(BASE_BRANCH) && !name.startsWith("tmp-merge-"))
                .collect(Collectors.toList());
    }

    public List<String> getBranchList(){
        try {
            return filterBranches(getBranches(projectPath));
        } catch (Exception e) {
            log.error("获取分支列表失败", e);
            return null;
        }
    }


    public void mergeBranch(String tmpBranch, List<String> branchesToMerge){
        try {
            createAndMergeBranches(tmpBranch, branchesToMerge);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
 

    /**
     * 克隆、创建临时分支并合并多个分支
     */
    private void createAndMergeBranches(String tmpBranch, List<String> branchesToMerge) throws Exception {
        File repoDir = new File(System.getProperty("java.io.tmpdir"), "tmpRepo/" + projectPath);
        if (repoDir.exists()) {
            deleteDir(repoDir);
        }
        if (!repoDir.mkdirs()) {
            throw new RuntimeException("创建临时目录失败: " + repoDir.getAbsolutePath());
        }
        String authRepoUrl = BASE_URL.replace("https://", "https://oauth2:" + token + "@");
        String repoUrl = authRepoUrl + "/" + projectPath +".git";
        execCommand(Arrays.asList("git", "clone", repoUrl, "."), repoDir);

        // 确保是干净状态
        execCommand(Arrays.asList("git", "fetch", "origin"), repoDir);

        // 判断临时分支是否存在
        boolean tmpBranchExists = false;
        String branches = execCommandAndReturnOutput(Arrays.asList("git", "branch", "-a"), repoDir);
        if (branches.contains(tmpBranch)) {
            tmpBranchExists = true;
        }
        // 根据分支是否存在选择逻辑
        if (tmpBranchExists) {
            log.info("临时分支已存在，无需重复创建：{}", tmpBranch);
            // 切到临时分支
            execCommand(Arrays.asList("git", "checkout", tmpBranch), repoDir);
        } else {
            log.info("临时分支不存在，新建临时分支：{}", tmpBranch);
            execCommand(Arrays.asList("git", "checkout", "-b", tmpBranch, "origin/" + BASE_BRANCH), repoDir);
        }

        // 逐个合并指定分支
        for (String branch : branchesToMerge) {
            log.info("开始合并分支: {}", branch);
            try {
                //  每次开始前先清理残留 merge 状态 并忽略忽略不存在 merge 的错误
                safeAbort(repoDir);
                execCommand(Arrays.asList("git", "reset", "--merge"), repoDir);
                // 执行 merge
                String mergeOutput = execCommandAndReturnOutput(Arrays.asList("git", "merge", "--no-commit", "--no-ff", "origin/" + branch), repoDir);
                // 检查是否已经合并过
                if (mergeOutput.contains("Already up to date.")) {
                    log.info("🔁 分支{}已merge，自动跳过。", branch);
                    continue;
                }
                execCommand(Arrays.asList("git", "commit", "-m", "Merge " + branch), repoDir);
                log.info("✅ 合并成功: {}" , branch);
            } catch (RuntimeException e) {
                log.info("合并失败:" , e);
                if (e.getMessage().contains("CONFLICT")) {
                    // 推送本地分支到远程，开发人员自行合并
                    safeAbort(repoDir);
                    execCommand(Arrays.asList("git", "push", "origin", tmpBranch), repoDir);
                    log.info("❌ 合并冲突: {}" , branch);
                    log.info("1：git fetch");
                    log.info("2：git checkout {}" , tmpBranch);
                    log.info("3：git merge --no-commit origin/{}", branch);
                    log.info("4：解决完冲突推送临时分支");
                    String errStr = "1：git fetch " + "\n2：git checkout " + tmpBranch +
                            "\n3：git merge --no-commit origin/" + branch +
                            "\n4：解决完冲突推送临时分支";
                    throw new RuntimeException("检测到冲突，请根据命令手动解决后再继续合并\n" + errStr);
                } else {
                    throw e;
                }
            }
        }
        // 推送中间分支
        execCommand(Arrays.asList("git", "push", "origin", tmpBranch), repoDir);
        log.info("临时发布分支合并成功，分支名：{}" , tmpBranch);
    }
    private void safeAbort(File repoDir) {
        try {
            execCommand(Arrays.asList("git", "merge", "--abort"), repoDir);
        } catch (Exception ex) {
            // 如果输出包含 MERGE_HEAD missing，则忽略
            if (!ex.getMessage().contains("MERGE_HEAD missing") && !ex.getMessage().contains("There is no merge to abort")) {
                throw new RuntimeException(ex);
            }
        }
    }
    /**
     * 执行命令行命令并返回控制台输出
     */
    private void execCommand(List<String> command, File workDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        reader.close();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("命令执行失败: " + String.join(" ", command) + "\n输出:\n" + output);
        }
    }
    private static String execCommandAndReturnOutput(List<String> command, File repoDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(repoDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        int exitCode = process.waitFor();
        if (exitCode != 0 && !output.contains("Already up to date")) {
            throw new RuntimeException("命令执行失败: " + String.join(" ", command) + "\n输出:\n" + output);
        }
        return output;
    }
}
