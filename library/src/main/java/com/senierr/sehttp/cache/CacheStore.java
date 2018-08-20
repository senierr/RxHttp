package com.senierr.sehttp.cache;

/**
 * 缓存存储接口
 *
 * @author zhouchunjie
 * @date 2018/8/18
 */
public interface CacheStore {

    <T> void put(String key, CacheEntity<T> cacheEntity);

    <T> CacheEntity<T> get(String key);
}
