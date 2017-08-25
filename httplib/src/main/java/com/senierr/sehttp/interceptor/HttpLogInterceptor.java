package com.senierr.sehttp.interceptor;

import android.util.Log;

import com.senierr.sehttp.util.SeLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;


public class HttpLogInterceptor implements Interceptor {

    public static final int PRINT_LEVEL_NONE = 100;      //不打印log
    public static final int PRINT_LEVEL_BASIC = 101;     //只打印 请求首行 和 响应首行
    public static final int PRINT_LEVEL_HEADERS = 102;   //打印请求和响应的所有 Header
    public static final int PRINT_LEVEL_BODY = 103;      //所有数据全部打印

    public static final int COLOR_LEVEL_VERBOSE = Log.VERBOSE;
    public static final int COLOR_LEVEL_DEBUG = Log.DEBUG;
    public static final int COLOR_LEVEL_INFO = Log.INFO;
    public static final int COLOR_LEVEL_WARN = Log.WARN;
    public static final int COLOR_LEVEL_ERROR = Log.ERROR;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private int printLevel;
    private int colorLevel;
    private String tag;

    /**
     * 打印日志的级别
     *
     * @param printLevel
     */
    public void setPrintLevel(int printLevel) {
        this.printLevel = printLevel;
    }

    /**
     * 打印日志的显示级别
     *
     * @param colorLevel
     */
    public void setColorLevel(int colorLevel) {
        this.colorLevel = colorLevel;
    }

    /**
     * 打印日志的tag
     *
     * @param tag
     */
    public void setPrintTag(String tag) {
        this.tag = tag;
    }

    public void log(String message) {
        Log.println(colorLevel, tag, message);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (printLevel == PRINT_LEVEL_NONE) {
            return chain.proceed(request);
        }

        //请求日志拦截
        logForRequest(request, chain.connection());

        //执行请求，计算请求时间
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log("║--> 请求失败: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        //响应日志拦截
        return logForResponse(response, tookMs);
    }

    private void logForRequest(Request request, Connection connection) throws IOException {
        boolean logBody = (printLevel == PRINT_LEVEL_BODY);
        boolean logHeaders = (printLevel == PRINT_LEVEL_BODY || printLevel == PRINT_LEVEL_HEADERS);
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;

        try {
            log(" -----------> 开始请求 <-----------");
            String requestStartMessage = "║\t" + request.method() + ' ' + request.url() + ' ' + protocol;
            log(requestStartMessage);
            if (logHeaders) {
                Headers headers = request.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    log("║\t" + headers.name(i) + ": " + headers.value(i));
                }

                log("║---------------------------------");
                if (logBody && hasRequestBody) {
                    if (isPlaintext(requestBody.contentType())) {
                        bodyToString(request);
                    } else {
                        log("║\tbody maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            SeLogger.e(e);
        } finally {
            log(" -----------> 结束请求 <-----------");
        }
    }

    private Response logForResponse(Response response, long tookMs) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        ResponseBody responseBody = clone.body();
        boolean logBody = (printLevel == PRINT_LEVEL_BODY);
        boolean logHeaders = (printLevel == PRINT_LEVEL_BODY || printLevel == PRINT_LEVEL_HEADERS);

        try {
            log(" -----------> 开始响应 <-----------");
            log("║\t" + clone.code() + ' ' + clone.message() + ' ' + clone.request().url() + " (" + tookMs + "ms）");
            if (logHeaders) {
                Headers headers = clone.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    log("║\t" + headers.name(i) + ": " + headers.value(i));
                }

                log("║---------------------------------");
                if (logBody && HttpHeaders.hasBody(clone)) {
                    if (isPlaintext(responseBody.contentType())) {
                        String body = responseBody.string();
                        log("║\t" + body);
                        responseBody = ResponseBody.create(responseBody.contentType(), body);
                        return response.newBuilder().body(responseBody).build();
                    } else {
                        log("║\tbody: maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            SeLogger.e(e);
        } finally {
            log(" -----------> 结束响应 <-----------");
        }
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            if (subtype.contains("x-www-form-urlencoded") ||
                subtype.contains("json") ||
                subtype.contains("xml") ||
                subtype.contains("html"))
                return true;
        }
        return false;
    }

    private void bodyToString(Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = copy.body().contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            log("║\t" + buffer.readString(charset));
        } catch (Exception e) {
            SeLogger.e(e);
        }
    }
}