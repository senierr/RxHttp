package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.Converter;
import com.senierr.sehttp.request.RequestBuilder;

/**
 * 请求回调基类
 *
 * Created by zhouchunjie on 2017/3/28.
 */

public abstract class BaseCallback<T> implements Converter<T> {

    public void onBefore(RequestBuilder requestBuilder) {}

    public void uploadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

    public void downloadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

    public abstract void onSuccess(T t, boolean isCache) throws Exception;

    public void onError(Exception e) {}

    public void onAfter() {}
}
