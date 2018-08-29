package com.senierr.http.converter;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSource;

/**
 * 字符串转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public class StringConverter implements Converter<String> {

    private Charset charset;

    public StringConverter(Charset charset) {
        this.charset = charset;
    }

    public String convertResponse(Response response) throws Throwable {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("ResponseBody is null!");
        }

        if (charset != null) {
            BufferedSource source = responseBody.source();
            try {
                return source.readString(charset);
            } finally {
                Util.closeQuietly(source);
            }
        } else {
            return responseBody.string();
        }
    }
}
