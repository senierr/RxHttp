package com.senierr.http.cookie

import com.senierr.http.cookie.store.CookieStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Cookie管理实现
 *
 * @author zhouchunjie
 * @date 2019/7/1
 */
class CookieJarImpl(private val cookieStore: CookieStore) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.saveCookies(url, cookies.toMutableList())
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookieStore.getCookies(url).forEach {
            if (cookieStore.isExpired(it)) {
                cookieStore.removeCookie(url, it)
            } else {
                cookies.add(it)
            }
        }
        return cookies
    }
}