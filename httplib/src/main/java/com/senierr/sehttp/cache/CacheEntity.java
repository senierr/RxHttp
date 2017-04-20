package com.senierr.sehttp.cache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 缓存实体
 *
 * @author zhouchunjie
 * @date 2017/4/19
 */

public class CacheEntity {

    private String key;
    private String cacheContent;
    private long updateDate;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCacheContent() {
        return cacheContent;
    }

    public void setCacheContent(String cacheContent) {
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
