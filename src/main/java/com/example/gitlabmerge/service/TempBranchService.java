package com.example.gitlabmerge.service;

import com.example.gitlabmerge.constants.RedisConstants;
import com.example.gitlabmerge.resp.TempBranchResp;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author kerwin
 */
@Service
public class TempBranchService {

    @Resource
    private RedisTemplate<String, TempBranchResp> redisTemplate;

    private String keyOf(String key) {
        return RedisConstants.TEMP_BRANCH_KEY_PREFIX + key;
    }

    public List<TempBranchResp> list(String key) {
        List<TempBranchResp> list = redisTemplate.opsForList().range(keyOf(key), 0, -1);
        return list == null ? Collections.emptyList() : list;
    }

    public void add(String key, TempBranchResp record) {
        if (record == null) {
            return;
        }
        String k = keyOf(key);
        redisTemplate.opsForList().leftPush(k, record);
        // 截断长度，最多保留 N 条
        Long size = redisTemplate.opsForList().size(k);
        if (size != null && size > RedisConstants.TEMP_BRANCH_MAX_PER_KEY) {
            long over = size - RedisConstants.TEMP_BRANCH_MAX_PER_KEY;
            for (int i = 0; i < over; i++) {
                redisTemplate.opsForList().rightPop(k);
            }
        }
    }

    public void update(String key, int index, TempBranchResp record) {
        String k = keyOf(key);
        redisTemplate.opsForList().set(k, index, record);
    }

    public TempBranchResp get(String key, int index) {
        String k = keyOf(key);
        return redisTemplate.opsForList().index(k, index);
    }

    public void delete(String key, int index) {
        String k = keyOf(key);
        // 方案：读取并重建（避免 Redis List 无法按索引删除的限制）
        List<TempBranchResp> list = redisTemplate.opsForList().range(k, 0, -1);
        if (CollectionUtils.isEmpty(list) || index < 0 || index >= list.size()) {
            return;
        }
        list.remove(index);
        redisTemplate.delete(k);
        if (CollectionUtils.isNotEmpty(list)) {
            // 按原顺序重新 LPUSH 到右端，确保顺序不变
            for (int i = list.size() - 1; i >= 0; i--) {
                redisTemplate.opsForList().leftPush(k, list.get(i));
            }
        }
    }

    public void clear(String key) {
        redisTemplate.delete(keyOf(key));
    }

    // ===== 通过 branchName 操作（无需知道 index） =====
    private int indexOfBranch(List<TempBranchResp> list, String branchName) {
        if (CollectionUtils.isEmpty(list)) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            TempBranchResp item = list.get(i);
            if (item != null && branchName != null && branchName.equals(item.getBranchName())) {
                return i;
            }
        }
        return -1;
    }

    public TempBranchResp getByBranchName(String key, String branchName) {
        String k = keyOf(key);
        List<TempBranchResp> list = redisTemplate.opsForList().range(k, 0, -1);
        int idx = indexOfBranch(list, branchName);
        return idx >= 0 ? list.get(idx) : null;
    }

    public void upsertByBranchName(String key, TempBranchResp record) {
        if (record == null || record.getBranchName() == null) {
            return;
        }
        String k = keyOf(key);
        List<TempBranchResp> list = redisTemplate.opsForList().range(k, 0, -1);
        int idx = indexOfBranch(list, record.getBranchName());
        if (idx >= 0) {
            // 更新
            redisTemplate.opsForList().set(k, idx, record);
        } else {
            // 新增并维持长度限制
            add(key, record);
        }
    }

    public void deleteByBranchName(String key, String branchName) {
        String k = keyOf(key);
        List<TempBranchResp> list = redisTemplate.opsForList().range(k, 0, -1);
        int idx = indexOfBranch(list, branchName);
        if (idx < 0) {
            return;
        }
        list.remove(idx);
        redisTemplate.delete(k);
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = list.size() - 1; i >= 0; i--) {
                redisTemplate.opsForList().leftPush(k, list.get(i));
            }
        }
    }

    public void updateStatusByBranchName(String key, String branchName, String status) {
        String k = keyOf(key);
        List<TempBranchResp> list = redisTemplate.opsForList().range(k, 0, -1);
        int idx = indexOfBranch(list, branchName);
        TempBranchResp target;
        if (idx >= 0) {
            target = list.get(idx);
        } else {
            target = new TempBranchResp();
            target.setBranchName(branchName);
        }
        target.setMergeStatus(status);
        upsertByBranchName(key, target);
    }
}


