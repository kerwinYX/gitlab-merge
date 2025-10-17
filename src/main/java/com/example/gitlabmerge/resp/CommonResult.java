package com.example.gitlabmerge.resp;

/**
 * @author kerwin
 * @date 2025/10/16 - 15:16
 **/
public class CommonResult<T> {
    public long code;
    public String message;
    public T data;

    protected CommonResult() {
    }

    public CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(0, "success", data);
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<>(0, "success", null);
    }
}
