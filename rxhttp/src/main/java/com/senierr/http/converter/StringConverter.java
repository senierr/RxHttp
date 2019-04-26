package com.senierr.http.converter;

import java.io.IOException;
import java.nio.charset.Charset;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
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

    private @Nullable Charset charset;

    public StringConverter() {
    }

    public StringConverter(@NonNull Charset charset) {
        this.charset = charset;
    }

    public StringConverter(@NonNull String charsetName) {
        this.charset = Charset.forName(charsetName);
    }

    public @NonNull String convertResponse(@NonNull Response response) throws Throwable {
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
