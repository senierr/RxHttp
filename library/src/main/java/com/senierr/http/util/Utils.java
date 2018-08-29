package com.senierr.http.util;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;

/**
 * 工具类
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public class Utils {

    /**
     * Map拼接
     *
     * @param oldMap
     * @param newMap
     * @return
     */
    public static <T> LinkedHashMap<String, T> appendMap(LinkedHashMap<String, T> oldMap, LinkedHashMap<String, T> newMap) {
        if (oldMap == null) {
            oldMap = new LinkedHashMap<>();
        }

        if (newMap == null || newMap.isEmpty()) {
            return oldMap;
        }

        for (String key: newMap.keySet()) {
            oldMap.put(key, newMap.get(key));
        }
        return oldMap;
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
