package com.senierr.http.cookie.store

import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * 内存存储
 *
 * @author zhouchunjie
 * @date 2018/8/14
 */
class MemoryCookieStore : CookieStore {

    private val memoryCookieMap = ConcurrentHashMap<String, MutableList<Cookie>>()

    @Synchronized
    override fun isExpired(cookie: Cookie): Boolean = cookie.expiresAt() < System.currentTimeMillis()

    @Synchronized
    override fun saveCookies(url: HttpUrl, cookies: MutableList<Cookie>) {
        val memoryCookies = memoryCookieMap[url.host()] ?: return
        val iterator = memoryCookies.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (cookies.contains(item)) {
                iterator.remove()
            }
        }
        memoryCookies.addAll(cookies)
    }

    @Synchronized
    override fun saveCookie(url: HttpUrl, cookie: Cookie) {
        val memoryCookies = memoryCookieMap[url.host()] ?: return
        val iterator = memoryCookies.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (cookie == item) {
                iterator.remove()
            }
        }
        memoryCookies.add(cookie)
    }

    @Synchronized
    override fun getCookies(url: HttpUrl): MutableList<Cookie> {
        val cookies = mutableListOf<Cookie>()
        val urlCookies = memoryCookieMap[url.host()]
        if (urlCookies != null) cookies.addAll(urlCookies)
        return cookies
    }

    @Synchronized
    override fun getAllCookie(): MutableList<Cookie> {
        val cookies = mutableListOf<Cookie>()
        memoryCookieMap.forEach {
            cookies.addAll(it.value)
        }
        return cookies
    }

    @Synchronized
    override fun removeCookie(url: HttpUrl, cookie: Cookie) {
        val memoryCookies = memoryCookieMap[url.host()] ?: return
        if (memoryCookies.contains(cookie)) {
            memoryCookies.remove(cookie)
        }
    }

    @Synchronized
    override fun removeCookies(url: HttpUrl) {
        if (memoryCookieMap.contains(url.host())) {
            memoryCookieMap.remove(url.host())
        }
    }

    @Synchronized
    override fun clear() {
        memoryCookieMap.clear()
    }
}