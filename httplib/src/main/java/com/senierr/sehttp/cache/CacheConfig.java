package com.senierr.sehttp.cache;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.util.FileUtil;

import java.io.File;

/**
 * 缓存配置
 *
 * @author zhouchunjie
 * @date 2017/4/19
 */

public class CacheConfig {

    public static final long DEFAULT_MAX_SIZE = 1024 * 1024 * 10;
    public static final long DEFAULT_CACHE_TIME = 1000 * 3600 * 24;

    // 缓存路径
    private File cacheFile;
    // 缓存大小
    private long maxSize;
    // 缓存有效时长
    private long cacheTime;

    private CacheConfig() {
    }

    public static CacheConfig build() {
        return new CacheConfig();
    }

    public File getCacheFile() {
        return cacheFile == null ? FileUtil.getCacheDirectory(
                SeHttp.getInstance().getApplication().getApplicationContext(), null) : cacheFile;
    }

    public CacheConfig cacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
        return this;
    }

    public long getMaxSize() {
        return maxSize == 0 ? DEFAULT_MAX_SIZE : maxSize;
    }

    public CacheConfig maxSize(long maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public long getCacheTime() {
        return cacheTime == 0 ? DEFAULT_CACHE_TIME : cacheTime;
    }

    public CacheConfig cacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }
}
