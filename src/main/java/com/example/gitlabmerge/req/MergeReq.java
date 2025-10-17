package com.example.gitlabmerge.req;

import lombok.Data;

import java.util.List;

/**
 * @author kerwin
 * @date 2025/10/16 - 17:26
 **/
@Data
public class MergeReq {
    private String project;
    private List<String> mergeBrancheList;
}
