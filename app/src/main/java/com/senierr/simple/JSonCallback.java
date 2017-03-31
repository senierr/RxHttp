package com.senierr.simple;

import com.senierr.sehttp.callback.BaseCallback;

import okhttp3.Response;

/**
 * @author zhouchunjie
 * @date 2017/3/31
 */

public class JSonCallback extends BaseCallback<String> {

    @Override
    public String convert(Response response) throws Exception {
        return null;
    }

    @Override
    public void onSuccess(String s) throws Exception {

    }

    @Override
    public void onError(Exception e) {

    }
}
