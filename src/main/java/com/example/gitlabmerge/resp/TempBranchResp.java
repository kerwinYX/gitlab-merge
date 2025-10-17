package com.example.gitlabmerge.resp;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author kerwin
 * @date 2025/10/16 - 18:37
 **/
@Data
@Accessors(chain = true)
public class TempBranchResp {
    private String branchName;

    private String mergeUser;

    private String mergeTime;

    private String mergeStatus;

    private String gitPath;


    private List<String> mergeBranchList;
}
