package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.Response;

/**
 * 返回结果，包括：
 *
 * 1. 上传进度
 * 2. 下载进度
 * 3. 解析结果
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public final class Result<T> {

    private @Nullable Progress uploadProgress;
    private @Nullable Progress downloadProgress;
    private @Nullable Response response;
    private @Nullable T body;

    private Result() {
    }

    public static <T> Result<T> upload(@NonNull Progress uploadProgress) {
        Result<T> result = new Result<>();
        result.uploadProgress = uploadProgress;
        return result;
    }

    public static <T> Result<T> download(@NonNull Progress downloadProgress) {
        Result<T> result = new Result<>();
        result.downloadProgress = downloadProgress;
        return result;
    }

    public static <T> Result<T> success(@NonNull Response response, T body) {
        Result<T> result = new Result<>();
        result.response = response;
        result.body = body;
        return result;
    }

    public @Nullable Progress uploadProgress() {
        return uploadProgress;
    }

    public @Nullable Progress downloadProgress() {
        return downloadProgress;
    }

    public @Nullable Response response() {
        return response;
    }

    public @Nullable T body() {
        return body;
    }
}