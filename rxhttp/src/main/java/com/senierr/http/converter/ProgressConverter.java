package com.senierr.http.converter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.internal.ProgressResponse;

import okhttp3.Response;

/**
 * 进度转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public class ProgressConverter<T> implements Converter<ProgressResponse<T>> {

    private Converter<T> rawConverter;

    public ProgressConverter(Converter<T> rawConverter) {
        this.rawConverter = rawConverter;
    }

    public @NonNull ProgressResponse<T> convertProgress(int type, long totalSize, long currentSize, int percent) throws Throwable {
        return new ProgressResponse<>(type, totalSize, currentSize, percent, null);
    }

    public @NonNull ProgressResponse<T> convertResponse(@NonNull Response response) throws Throwable {
        return new ProgressResponse<>(ProgressResponse.TYPE_RESULT, -1, -1, -1, rawConverter.convertResponse(response));
    }
}
