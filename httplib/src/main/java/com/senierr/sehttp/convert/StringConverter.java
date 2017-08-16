package com.senierr.sehttp.convert;

import okhttp3.Response;

/**
 * String类型解析
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class StringConverter implements Converter<String> {

    @Override
    public String convert(Response response) throws Exception {
        return response.body().string();
    }
}
