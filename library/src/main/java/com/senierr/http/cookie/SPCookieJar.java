package com.senierr.http.cookie;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * SharedPreferences存储器
 *
 * @author zhouchunjie
 * @date 2018/8/14
 */
public final class SPCookieJar extends ClearableCookieJar {

    private static final String COOKIE_PREFS = "cookie_store";
    private static final String COOKIE_PREFIX = "cookie_";

    private final SharedPreferences cookiePrefs;
    private List<Cookie> cookieCache;

    public SPCookieJar(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE);
        cookieCache = new ArrayList<>();
        // 文件同步至内存
        for (Map.Entry<String, ?> entry : cookiePrefs.getAll().entrySet()) {
            if (entry.getValue() != null && entry.getKey().startsWith(COOKIE_PREFIX)) {
                String serializedCookie = (String) entry.getValue();
                Cookie cookie = SerializableCookie.decode(serializedCookie);
                if (cookie != null) {
                    cookieCache.add(cookie);
                }
            }
        }
    }

    @Override
    public void saveCookies(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            saveCookie(url, cookie);
        }
    }

    @Override
    public void saveCookie(HttpUrl url, Cookie cookie) {
        // 过期无需缓存
        if (isCookieExpired(cookie)) {
            return;
        }
        // 内存缓存
        List<Cookie> needRemove = new ArrayList<>();
        for (Cookie item : cookieCache) {
            if (cookie.name().equals(item.name())) {
                needRemove.add(item);
            }
        }
        cookieCache.removeAll(needRemove);
        cookieCache.add(cookie);
        // 文件缓存
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.putString(getCookiePrefsKey(cookie), SerializableCookie.encode(cookie));
        prefsWriter.apply();
    }

    @Override
    public List<Cookie> getCookies(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        for (Cookie cookie : cookieCache) {
            if (cookie.matches(url)) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    @Override
    public List<Cookie> getAllCookie() {
        return cookieCache;
    }

    @Override
    public void removeCookie(HttpUrl url, Cookie cookie) {
        // 内存移除
        cookieCache.remove(cookie);
        // 文件移除
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.remove(getCookiePrefsKey(cookie));
        prefsWriter.apply();
    }

    @Override
    public void removeCookies(HttpUrl url) {
        // 内存移除
        List<Cookie> needRemove = new ArrayList<>();
        for (Cookie item : cookieCache) {
            if (item.matches(url)) {
                needRemove.add(item);
            }
        }
        cookieCache.removeAll(needRemove);
        // 文件移除
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        for (Cookie cookie : needRemove) {
            prefsWriter.remove(getCookiePrefsKey(cookie));
        }
        prefsWriter.apply();
    }

    @Override
    public void clear() {
        // 内存移除
        cookieCache.clear();
        // 文件移除
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.clear();
        prefsWriter.apply();
    }

    private String getCookiePrefsKey(Cookie cookie) {
        return COOKIE_PREFIX + cookie.name()
                + "@" + (cookie.secure() ? "https" : "http")
                + "://" + cookie.domain() + cookie.path();
    }
}
