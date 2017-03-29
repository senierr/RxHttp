package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.Converter;

/**
 * 请求回调基类
 *
 * Created by zhouchunjie on 2017/3/28.
 */

public abstract class BaseCallback<T> implements Converter<T> {

    public void onStart() {}

    public abstract void onSuccess(T t) throws Exception;

    public abstract void onError(Exception e);

    public void onAfter() {}
}
