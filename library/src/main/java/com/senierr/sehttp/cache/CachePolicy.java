package com.senierr.sehttp.cache;

/**
 * 缓存类型
 *
 * @author zhouchunjie
 * @date 2018/8/18
 */
public enum CachePolicy {
    NO_CACHE,           // 不缓存
    REQUEST_ELSE_CACHE, // 优先请求网络，若失败，使用缓存
    CACHE_ELSE_REQUEST, // 优先使用缓存，若无，请求网络
    CACHE_THEN_REQUEST  // 先使用缓存，然后请求网络
}
