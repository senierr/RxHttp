package com.senierr.simple;

import com.senierr.http.callback.Callback;
import com.senierr.http.converter.Converter;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 自定义解析回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public abstract class MyCallback extends Callback<MyEntity> {

    public MyCallback() {
        super(new MyConverter());
    }

    /** 自定义转换器 */
    static class MyConverter implements Converter<MyEntity> {

        @Override
        public MyEntity convertResponse(Response response) throws Throwable {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("ResponseBody is null!");
            }

            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONObject data = jsonObject.getJSONObject("data");
            return new MyEntity(data.getString("ip"),
                    data.getString("country"),
                    data.getString("city"),
                    data.getString("isp"));
        }
    }
}
