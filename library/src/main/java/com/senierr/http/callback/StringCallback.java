package com.senierr.http.callback;

import com.senierr.http.converter.StringConverter;

import java.nio.charset.Charset;

/**
 * 字符串类型回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public abstract class StringCallback extends Callback<String> {

    public StringCallback() {
        this(null);
    }

    public StringCallback(Charset charset) {
        super(new StringConverter(charset));
    }
}
