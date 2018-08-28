package com.senierr.sehttp.cache;

import java.io.Serializable;

/**
 * 缓存实体
 *
 * @author zhouchunjie
 * @date 2018/8/20
 */
public final class CacheEntity<T> implements Serializable {

    public static final long DEFAULT_CACHE_DURATION = 1000 * 60 * 60 * 24;   // 24小时

    private static final long serialVersionUID = -4337711009801627866L;

    private String cacheKey;
    private CachePolicy cachePolicy;
    private long cacheDuration;
    private T content;
    private long cacheTime;

    public CacheEntity(String cacheKey, CachePolicy cachePolicy, long cacheDuration) {
        this.cacheKey = cacheKey;
        this.cachePolicy = cachePolicy;
        this.cacheDuration = cacheDuration;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    public void setCachePolicy(CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    @Override
    public String toString() {
        return "CacheEntity{" +
                "cacheKey='" + cacheKey + '\'' +
                ", cachePolicy=" + cachePolicy +
                ", cacheDuration=" + cacheDuration +
                ", content=" + content +
                ", cacheTime=" + cacheTime +
                '}';
    }

    /**
     * 是否过期
     *
     * @return
     */
    public boolean isExpired() {
        return cacheTime + cacheDuration >= System.currentTimeMillis();
    }
}
