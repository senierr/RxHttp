package com.senierr.sehttp.convert;

import okhttp3.Response;

/**
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class StringConverter implements Converter<String> {

    @Override
    public String convert(Response response) throws Exception {
        int responseCode = response.code();
        if (!response.isSuccessful()) {
            throw new Exception("Response is not successful! responseCode: " + responseCode);
        }
        return response.body().string();
    }
}
