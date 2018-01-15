package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.StringConverter;

import java.nio.charset.Charset;

import okhttp3.Response;

/**
 * 字符串类型回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class StringCallback extends BaseCallback<String> {

    private Charset charset;

    public StringCallback() {
    }

    public StringCallback(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String convert(Response response) throws Exception {
        return new StringConverter(charset).convert(response);
    }
}
