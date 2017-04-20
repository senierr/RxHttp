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

    public static final long DEFAULT_MAX_SIZE = 1024 * 1024;
    public static final long DEFAULT_CACHE_TIME = 1000 * 3600 * 24;

    // 缓存路径
    private File cacheFile;
    // 单个缓存最大长度
    private long maxSize;

    public CacheConfig() {
    }

    public CacheConfig(File cacheFile, long maxSize) {
        this.cacheFile = cacheFile;
        this.maxSize = maxSize;
    }

    public File getCacheFile() {
        if (cacheFile == null) {
            cacheFile = FileUtil.getCacheDirectory(
                    SeHttp.getInstance().getApplication().getApplicationContext(), null);
        }
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public long getMaxSize() {
        return maxSize == 0 ? DEFAULT_MAX_SIZE : maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
}
