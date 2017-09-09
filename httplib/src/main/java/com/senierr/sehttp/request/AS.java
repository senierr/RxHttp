package com.senierr.sehttp.request;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * @author zhouchunjie
 * @date 2017/9/9
 */

public class AS extends RequestBody {
    public AS() {
        super();
    }

    @Override
    public long contentLength() throws IOException {
        return super.contentLength();
    }

    @Override
    public MediaType contentType() {
        return null;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

    }
}
