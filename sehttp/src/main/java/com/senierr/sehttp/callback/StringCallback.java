package com.senierr.sehttp.callback;

import android.os.Handler;

import com.senierr.sehttp.callback.BaseCallback;

import okhttp3.Response;

/**
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class StringCallback extends BaseCallback<String> {

    @Override
    public void convert(final Response response, Handler mainScheduler) throws Exception {
        final int responseCode = response.code();
        if (responseCode != 200) {
            mainScheduler.post(new Runnable() {
                @Override
                public void run() {
                    onError(responseCode, null);
                    onAfter();
                }
            });
        } else {
            final String result = response.body().string();
            response.close();
            mainScheduler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        onSuccess(result);
                    } catch (Exception e) {
                        onError(-1, e);
                    } finally {
                        onAfter();
                    }
                }
            });
        }
    }
}
