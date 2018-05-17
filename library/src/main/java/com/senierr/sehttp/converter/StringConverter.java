package com.senierr.sehttp.converter;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSource;

/**
 * String转换器
 *
 * @author zhouchunjie
 * @date 2018/5/17
 */
public class StringConverter implements Converter<String> {

    private Charset charset;

    public StringConverter() {
    }

    public StringConverter(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String onConvert(Response response) throws Exception {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("ResponseBody is null");
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
