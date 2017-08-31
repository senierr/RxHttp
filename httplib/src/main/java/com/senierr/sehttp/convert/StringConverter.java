package com.senierr.sehttp.convert;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * String类型解析
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class StringConverter implements Converter<String> {

    @Override
    public String convert(Response response) throws Exception {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("ResponseBody is null");
        }
        return responseBody.string();
    }
}
