package com.senierr.http.cookie;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Cookie存储扩展
 *
 * @author zhouchunjie
 * @date 2018/8/14
 */
public abstract class ClearableCookieJar implements CookieJar {

    public boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    @Override
    final public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        saveCookies(url, cookies);
    }

    @Override
    final public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        List<Cookie> cookies = getCookies(url);
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (isCookieExpired(cookie)) {
                removeCookie(url, cookie);
            } else {
                result.add(cookie);
            }
        }
        return result;
    }

    public abstract void saveCookies(HttpUrl url, List<Cookie> cookies);

    public abstract void saveCookie(HttpUrl url, Cookie cookie);

    public abstract List<Cookie> getCookies(HttpUrl url);

    public abstract List<Cookie> getAllCookie();

    public abstract void removeCookie(HttpUrl url, Cookie cookie);

    public abstract void removeCookies(HttpUrl url);

    public abstract void clear();
}
