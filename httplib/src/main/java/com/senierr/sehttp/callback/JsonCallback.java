package com.senierr.sehttp.callback;

import okhttp3.Response;

/**
 * Json类型回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class JsonCallback<T> extends BaseCallback<T> {

    @Override
    public T convert(Response response) throws Exception {
        return null;
    }
}
