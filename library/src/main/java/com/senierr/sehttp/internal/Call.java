package com.senierr.sehttp.internal;

import com.senierr.sehttp.callback.Callback;

public interface Call<T> extends okhttp3.Call {

    void enqueue(Callback<T> callback);
}
