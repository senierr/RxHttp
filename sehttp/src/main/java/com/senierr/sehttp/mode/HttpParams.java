package com.senierr.sehttp.mode;

import java.util.Map;

/**
 * 请求URL参数
 *
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
    public static String buildParams(String url, Map<String, String> urlParams){
        if (urlParams != null && !urlParams.isEmpty()) {
            StringBuilder strParams = new StringBuilder();
            if (url.contains("?")) {
                strParams.append("&");
            } else {
                strParams.append("?");
            }

            for (String key: urlParams.keySet()) {
                strParams.append("&").append(key).append("=").append(urlParams.get(key));
            }

            strParams.deleteCharAt(1);
            if (url.indexOf("?") == url.length() - 1) {
                strParams.deleteCharAt(0);
            }

            strParams.insert(0, url);
            url = strParams.toString();
        }
        return url;
    }
}
