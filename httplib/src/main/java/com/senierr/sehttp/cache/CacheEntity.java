package com.senierr.sehttp.cache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 缓存实体
 *
 * @author zhouchunjie
 * @date 2017/4/19
 */

public class CacheEntity<T> implements Serializable {

    private static final long serialVersionUID = -4337711009801627866L;

    private String key;
    private T cacheContent;
    private long updateDate;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getCacheContent() {
        return cacheContent;
    }

    public void setCacheContent(T cacheContent) {
        this.cacheContent = cacheContent;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public String toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", key);
            jsonObject.put("cacheContent", cacheContent);
            jsonObject.put("updateDate", updateDate);
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static CacheEntity parseJson(String jsonStr) {
        try {
            CacheEntity cacheEntity = new CacheEntity();
            JSONObject jsonObject = new JSONObject(jsonStr);
            cacheEntity.setKey(jsonObject.getString("key"));
            cacheEntity.setCacheContent(jsonObject.getString("cacheContent"));
            cacheEntity.setUpdateDate(jsonObject.getLong("updateDate"));
            return cacheEntity;
        } catch (JSONException e) {
            return null;
        }
    }
}
