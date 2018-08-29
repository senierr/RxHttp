package com.senierr.http.util;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;

/**
 * http工具类
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public class Utils {

    /**
     * 创建请求头部
     *
     * @param headerParams
     * @return
     */
    public static Headers buildHeaders(Map<String, String> headerParams){
        Headers headers = null;
        if (headerParams != null) {
            Headers.Builder builder = new Headers.Builder();
            for (String key: headerParams.keySet()) {
                builder.add(key, headerParams.get(key));
            }
            headers = builder.build();
        }
        return headers;
    }

    /**
     * 合并并生成新Map
     *
     * @param oldMap
     * @param newMap
     * @return
     */
    public static <T> LinkedHashMap<String, T> mergeMap(LinkedHashMap<String, T> oldMap, LinkedHashMap<String, T> newMap) {
        LinkedHashMap<String, T> mergeMap = new LinkedHashMap<>();
        if (oldMap != null) {
            for (String key: oldMap.keySet()) {
                mergeMap.put(key, oldMap.get(key));
            }
        }
        if (newMap != null) {
            for (String key: newMap.keySet()) {
                mergeMap.put(key, newMap.get(key));
            }
        }
        return mergeMap;
    }

    /**
     * 根据文件名，判断类型
     *
     * @param path
     * @return
     */
    public static MediaType guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        path = path.replace("#", "");   //解决文件名中含有#号异常的问题
        String contentType = fileNameMap.getContentTypeFor(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }
}
