package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.StringConverter;

import okhttp3.Response;

/**
 * 字符串类型回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class StringCallback extends BaseCallback<String> {

    @Override
    public String convert(Response response) throws Exception {
        return new StringConverter().convert(response);
    }
}
