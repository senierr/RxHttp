package com.senierr.sehttp.convert;

import com.senierr.sehttp.callback.JsonCallback;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSource;

/**
 * JSON类型解析
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class JsonConverter<T> implements Converter<T> {

    private Charset charset;
    private JsonCallback<T> jsonCallback;

    public JsonConverter(Charset charset, JsonCallback<T> jsonCallback) {
        this.charset = charset;
        this.jsonCallback = jsonCallback;
    }

    @Override
    public T convert(Response response) throws Exception {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("ResponseBody is null");
        }

        if (charset != null) {
            BufferedSource source = responseBody.source();
            try {
                return jsonCallback.parseJson(source.readString(charset));
            } finally {
                Util.closeQuietly(source);
            }
        } else {
            return jsonCallback.parseJson(responseBody.string());
        }
    }
}
