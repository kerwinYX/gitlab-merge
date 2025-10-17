package com.example.gitlabmerge.resp;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author kerwin
 * @date 2025/10/17 - 14:35
 **/
@Data
@Accessors(chain = true)
public class MergeResp {
    private String mergeUser;

    private String mergeStatus;

    private String mergeMsg;
    private String tempBranch;
}
