package com.senierr.sehttp.cookie;

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
public abstract class ExpandCookieJar implements CookieJar {

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
        for (Cookie cookie : cookies) {
            if (isCookieExpired(cookie)) {
                removeCookie(url, cookie);
            } else {
                result.add(cookie);
            }
        }
        return result;
    }

    abstract void saveCookies(HttpUrl url, List<Cookie> cookies);

    abstract void saveCookie(HttpUrl url, Cookie cookie);

    abstract List<Cookie> getCookies(HttpUrl url);

    abstract List<Cookie> getAllCookie();

    abstract boolean removeCookie(HttpUrl url, Cookie cookie);

    abstract boolean removeCookies(HttpUrl url);

    abstract boolean removeAllCookie();
}
