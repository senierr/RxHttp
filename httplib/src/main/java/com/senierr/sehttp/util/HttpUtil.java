package com.senierr.sehttp.util;

import com.senierr.sehttp.mode.FileMap;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import okhttp3.MediaType;

/**
 * @author zhouchunjie
 * @date 2017/3/28
 */

public class HttpUtil {

    /**
     * map拼接
     *
     * @param oldMap
     * @param newMap
     * @return
     */
    public static LinkedHashMap<String, String> appendStringMap(LinkedHashMap<String, String> oldMap, LinkedHashMap<String, String> newMap) {
        if (newMap == null || newMap.isEmpty()) {
            return oldMap;
        }
        if (oldMap == null) {
            oldMap = new LinkedHashMap<>();
        }
        for (String key: newMap.keySet()) {
            oldMap.put(key, newMap.get(key));
        }
        return oldMap;
    }

    /**
     * map拼接
     *
     * @param oldMap
     * @param newMap
     * @return
     */
    public static FileMap appendFileMap(FileMap oldMap, FileMap newMap) {
        if (newMap == null || newMap.isEmpty()) {
            return oldMap;
        }
        if (oldMap == null) {
            oldMap = new FileMap();
        }
        oldMap.addAll(newMap);
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
