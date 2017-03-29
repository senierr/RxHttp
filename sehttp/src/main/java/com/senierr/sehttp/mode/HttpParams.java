package com.senierr.sehttp.mode;

import java.util.Map;

/**
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class HttpParams {

    /**
     * 创建URL参数
     *
     * @param urlParams
     * @return
     */
    public static String buildParams(Map<String, String> urlParams){
        StringBuilder strParams = new StringBuilder();
        if (urlParams != null) {
            strParams.append("?");
            for (String key: urlParams.keySet()) {
                strParams.append("&");
                strParams.append(key);
                strParams.append("=");
                strParams.append(urlParams.get(key));
            }
            strParams.deleteCharAt(1);
        }
        return strParams.toString();
    }
}
