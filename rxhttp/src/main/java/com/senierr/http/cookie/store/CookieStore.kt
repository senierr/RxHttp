package com.senierr.http.cookie.store

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * Cookie仓库接口
 *
 * @author zhouchunjie
 * @date 2018/11/07
 */
interface CookieStore {

    fun isExpired(cookie: Cookie): Boolean

    fun saveCookies(url: HttpUrl, cookies: MutableList<Cookie>)

    fun saveCookie(url: HttpUrl, cookie: Cookie)

    fun getCookies(url: HttpUrl): MutableList<Cookie>

    fun getAllCookie(): MutableList<Cookie>

    fun removeCookie(url: HttpUrl, cookie: Cookie)

    fun removeCookies(url: HttpUrl)

    fun clear()
}