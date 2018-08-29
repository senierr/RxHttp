package com.senierr.http.internal;

import com.senierr.http.listener.OnProgressListener;

public interface Call<T> extends okhttp3.Call {

    void enqueue(OnProgressListener<T> callback);
}
