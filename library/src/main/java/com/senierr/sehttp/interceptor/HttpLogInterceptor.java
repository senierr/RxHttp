package com.senierr.sehttp.interceptor;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * 日志拦截器
 *
 * @author zhouchunjie
 * @date 2017/9/8
 */

public class HttpLogInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private String tag;
    private LogLevel logLevel;

    public HttpLogInterceptor(String tag, LogLevel logLevel) {
        this.tag = tag;
        this.logLevel = logLevel;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (logLevel == LogLevel.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = (logLevel == LogLevel.BODY);
        boolean logHeaders = (logBody || logLevel == LogLevel.HEADERS);

        Request copyRequest = request.newBuilder().build();
        RequestBody requestBody = copyRequest.body();
        boolean hasRequestBody = requestBody != null;

        log(" ----------------------> 开始请求 <----------------------");
        Connection connection = chain.connection();
        String requestStartMessage = "\u007C " + copyRequest.method()
                + " " + copyRequest.url()
                + " " + (connection != null ? connection.protocol() : "");
        log(requestStartMessage);

        if (logHeaders) {
            log("\u007C Headers:");
            Headers headers = copyRequest.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                log("\u007C     " + headers.name(i) + ": " + headers.value(i));
            }

            if (logBody && hasRequestBody) {
                log("\u007C Body:");
                if (isPlaintext(requestBody.contentType())) {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);

                    Charset charset = UTF8;
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }
                    if (charset != null) {
                        log("\u007C     " + buffer.readString(charset));
                    }
                } else {
                    log("\u007C     Body maybe [file part] , too large too print , ignored!");
                }
            }
        }

        log(" ----------------------> 结束请求 <----------------------");

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log("\u007C--> 请求失败: " + e.toString());
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        log(" ----------------------> 开始响应 <----------------------");
        Response.Builder builder = response.newBuilder();
        Response cloneResponse = builder.build();

        log("\u007C " + cloneResponse.code()
                + " " + cloneResponse.message()
                + " " + cloneResponse.request().url()
                + " (" + tookMs + "ms)");

        if (logHeaders) {
            log("\u007C Headers:");
            Headers headers = cloneResponse.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                log("\u007C     " + headers.name(i) + ": " + headers.value(i));
            }

            if (logBody && HttpHeaders.hasBody(cloneResponse)) {
                log("\u007C Body:");
                ResponseBody responseBody = cloneResponse.body();
                if (responseBody != null && isPlaintext(responseBody.contentType())) {
                    String body = responseBody.string();
                    log("\u007C     " + body);
                    log(" ----------------------> 结束响应 <----------------------");
                    responseBody = ResponseBody.create(responseBody.contentType(), body);
                    return response.newBuilder().body(responseBody).build();
                } else {
                    log("\u007C     body: maybe [file part] , too large too print , ignored!");
                }
            }
        }
        log(" ----------------------> 结束响应 <----------------------");

        return response;
    }

    /**
     * 判断body是否是文本内容
     *
     * @param mediaType
     * @return
     */
    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            return subtype.contains("x-www-form-urlencoded") ||
                    subtype.contains("json") ||
                    subtype.contains("xml") ||
                    subtype.contains("plain") ||
                    subtype.contains("html");
        }
        return false;
    }

    /**
     * 日志打印
     *
     * @param message
     */
    private void log(String message) {
        Log.println(Log.DEBUG, tag, message);
    }
}
