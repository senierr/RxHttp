package com.senierr.sehttp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouchunjie
 * @date 2017/3/28
 */

public class HttpUtil {

    public static Map<String, String> appendMap(Map<String, String> oldMap, Map<String, String> newMap) {
        if (newMap == null || newMap.isEmpty()) {
            return oldMap;
        }
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        for (String key: newMap.keySet()) {
            oldMap.put(key, newMap.get(key));
        }
        return oldMap;
    }

}
