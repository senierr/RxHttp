package com.senierr.http.internal;

import com.senierr.http.callback.Callback;

public interface Call<T> extends okhttp3.Call {

    void enqueue(Callback<T> callback);
}
