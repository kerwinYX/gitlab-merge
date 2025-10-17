//package com.example.gitlabmerge.controller;
//
//import com.example.gitlabmerge.resp.CommonResult;
//import com.example.gitlabmerge.resp.TempBranchResp;
//import com.example.gitlabmerge.service.TempBranchService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/temp-branch")
//@RequiredArgsConstructor
//public class TempBranchController {
//
//    private final TempBranchService tempBranchService;
//
//    @GetMapping("/{key}")
//    public CommonResult<List<TempBranchResp>> list(@PathVariable("key") String key) {
//        return CommonResult.success(tempBranchService.list(key));
//    }
//
//    @PostMapping("/{key}")
//    public CommonResult<Void> add(@PathVariable("key") String key, @RequestBody TempBranchResp body) {
//        tempBranchService.add(key, body);
//        return CommonResult.success();
//    }
//
//    @GetMapping("/{key}/{index}")
//    public CommonResult<TempBranchResp> get(@PathVariable("key") String key, @PathVariable("index") int index) {
//        return CommonResult.success(tempBranchService.get(key, index));
//    }
//
//    @PutMapping("/{key}/{index}")
//    public CommonResult<Void> update(@PathVariable("key") String key, @PathVariable("index") int index,
//                                     @RequestBody TempBranchResp body) {
//        tempBranchService.update(key, index, body);
//        return CommonResult.success();
//    }
//
//    @DeleteMapping("/{key}/{index}")
//    public CommonResult<Void> delete(@PathVariable("key") String key, @PathVariable("index") int index) {
//        tempBranchService.delete(key, index);
//        return CommonResult.success();
//    }
//
//    @DeleteMapping("/{key}")
//    public CommonResult<Void> clear(@PathVariable("key") String key) {
//        tempBranchService.clear(key);
//        return CommonResult.success();
//    }
//
//    // ===== 按 branchName 操作（无需 index） =====
//    @GetMapping("/{key}/by-name/{branchName}")
//    public CommonResult<TempBranchResp> getByName(@PathVariable("key") String key,
//                                                  @PathVariable("branchName") String branchName) {
//        return CommonResult.success(tempBranchService.getByBranchName(key, branchName));
//    }
//
//    @PutMapping("/{key}/by-name")
//    public CommonResult<Void> upsertByName(@PathVariable("key") String key,
//                                           @RequestBody TempBranchResp body) {
//        tempBranchService.upsertByBranchName(key, body);
//        return CommonResult.success();
//    }
//
//    @DeleteMapping("/{key}/by-name/{branchName}")
//    public CommonResult<Void> deleteByName(@PathVariable("key") String key,
//                                           @PathVariable("branchName") String branchName) {
//        tempBranchService.deleteByBranchName(key, branchName);
//        return CommonResult.success();
//    }
//}
//
//
