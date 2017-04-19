package com.senierr.sehttp.cache;

/**
 * 缓存模式
 *
 * 请求网络，不使用缓存
 *
 * 请求网络，成功使用网络，失败使用缓存
 *
 * @author zhouchunjie
 * @date 2017/4/19
 */

public class CacheMode {
    public static int NO_CACHE = 0;
    public static int REQUEST_FAILED_READ_CACHE = 1;
}
