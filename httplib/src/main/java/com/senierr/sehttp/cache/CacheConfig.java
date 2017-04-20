package com.senierr.sehttp.cache;

import java.io.File;

/**
 * 缓存配置
 *
 * @author zhouchunjie
 * @date 2017/4/19
 */

public class CacheConfig {

    // 缓存路径
    private File cacheFile;
    // 单个缓存最大长度
    private long maxSize;

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
}
