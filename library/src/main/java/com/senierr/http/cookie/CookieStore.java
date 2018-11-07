package com.senierr.http.cookie;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Cookie仓库接口
 *
 * @author zhouchunjie
 * @date 2018/11/07
 */
public interface CookieStore {

    CookieJar getCookieJar();

    boolean isExpired(Cookie cookie);

    void saveCookies(HttpUrl url, List<Cookie> cookies);

    void saveCookie(HttpUrl url, Cookie cookie);

    List<Cookie> getCookies(HttpUrl url);

    List<Cookie> getAllCookie();

    void removeCookie(HttpUrl url, Cookie cookie);

    void removeCookies(HttpUrl url);

    void clear();
}
