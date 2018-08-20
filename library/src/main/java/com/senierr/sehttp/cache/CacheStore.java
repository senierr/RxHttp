package com.senierr.sehttp.cache;

import java.io.Serializable;

/**
 * 缓存存储接口
 *
 * @author zhouchunjie
 * @date 2018/8/18
 */
public interface CacheStore {

    void put(String key, Serializable value);

    <T> T get(String key);
}
