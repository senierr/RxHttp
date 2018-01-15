package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.JsonConverter;

import java.nio.charset.Charset;

import okhttp3.Response;

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
        return new JsonConverter<>(charset, this).convert(response);
    }
}
