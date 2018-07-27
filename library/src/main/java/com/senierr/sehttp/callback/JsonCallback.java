package com.senierr.sehttp.callback;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSource;

/**
 * JSON类型回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class JsonCallback<T> extends BaseCallback<T> {

    private Charset charset;

    public JsonCallback() {}

    public JsonCallback(Charset charset) {
        this.charset = charset;
    }

    /**
     * JSON解析
     *
     * 注：异步线程
     *
     * @param responseStr 待解析字符串
     * @return 解析结果
     * @throws Exception 解析失败异常
     */
    public abstract T parseJson(String responseStr) throws Exception;

    @Override
    public T convert(Response response) throws Exception {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("ResponseBody is null");
        }

        if (charset != null) {
            BufferedSource source = responseBody.source();
            try {
                return parseJson(source.readString(charset));
            } finally {
                Util.closeQuietly(source);
            }
        } else {
            return parseJson(responseBody.string());
        }
    }
}
