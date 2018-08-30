package com.senierr.simple;

import android.support.annotation.NonNull;

import com.senierr.http.converter.Converter;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 自定义数据转换器
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public class MyConverter implements Converter<MyEntity> {

    @Override
    public @NonNull MyEntity convertResponse(@NonNull Response response) throws Throwable {
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
