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
    // 缓存有效时间
    private long maxTime;

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }
}
