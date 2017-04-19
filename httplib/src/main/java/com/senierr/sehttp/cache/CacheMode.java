package com.senierr.sehttp.cache;

/**
 * 缓存模式
 *
 * @author zhouchunjie
 * @date 2017/4/19
 */

public enum  CacheMode {
    // 不缓存
    NO_CACHE,
    // 先请求网络，成功则使用网络，失败读取缓存
    REQUEST_FAILED_CACHE,
    // 先读取缓存，成功则使用缓存，失败请求网络
    CACHE_FAILED_REQUEST,
    // 先读取缓存，无论成功与否，然后请求网络
    CACHE_THEN_REQUEST
}
