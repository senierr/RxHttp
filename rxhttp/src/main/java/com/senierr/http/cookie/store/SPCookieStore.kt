package com.senierr.http.cookie.store

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.senierr.http.cookie.SerializableCookie
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * SharedPreferences存储
 *
 * @author zhouchunjie
 * @date 2018/11/07
 */
class SPCookieStore(context: Context) : CookieStore {

    companion object {
        private const val COOKIE_PREFS = "cookie_store"           //cookie使用prefs保存
        private const val COOKIE_NAME_PREFIX = "cookie_"         //cookie持久化的统一前缀
    }

    /**
     * <url.host, <cookieToken, Cookie>>
     */
    private val cookies: MutableMap<String, ConcurrentHashMap<String, Cookie>>
    /**
     * <url.host, "cookieToken1,...,cookieTokenN">
     * <cookie_cookieToken, encodeCookie>
     */
    private val cookiePrefs: SharedPreferences

    init {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE)
        cookies = mutableMapOf()

        //将持久化的cookies缓存到内存中,数据结构为 Map<Url.host, Map<CookieToken, Cookie>>
        val prefsMap = cookiePrefs.all
        for ((key, value) in prefsMap) {
            if (value != null && !key.startsWith(COOKIE_NAME_PREFIX)) {
                //获取url对应的所有cookie的key,用","分割
                val cookieTokens = TextUtils.split(value as String, ",")
                for (cookieToken in cookieTokens) {
                    //根据对应cookie的Key,从xml中获取cookie的真实值
                    val encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + cookieToken, null)
                    if (encodedCookie != null) {
                        val decodedCookie = SerializableCookie.decode(encodedCookie)
                        if (decodedCookie != null) {
                            if (!cookies.containsKey(key)) {
                                cookies[key] = ConcurrentHashMap()
                            }
                            cookies[key]?.put(cookieToken, decodedCookie)
                        }
                    }
                }
            }
        }
    }

    @Synchronized
    override fun isExpired(cookie: Cookie): Boolean = cookie.expiresAt < System.currentTimeMillis()

    @Synchronized
    override fun saveCookies(url: HttpUrl, cookies: MutableList<Cookie>) {
        for (cookie in cookies) {
            saveCookie(url, cookie)
        }
    }

    @Synchronized
    override fun saveCookie(url: HttpUrl, cookie: Cookie) {
        if (!cookies.containsKey(url.host)) {
            cookies[url.host] = ConcurrentHashMap()
        }
        //当前cookie是否过期
        if (isExpired(cookie)) {
            removeCookie(url, cookie)
        } else {
            val cookieToken = getCookieToken(cookie)
            //内存缓存
            cookies[url.host]?.put(cookieToken, cookie)
            //文件缓存
            val prefsWriter = cookiePrefs.edit()
            // 增加索引
            prefsWriter.putString(url.host, formatCookieTokens(url.host))
            // 增加值
            prefsWriter.putString(COOKIE_NAME_PREFIX + cookieToken, SerializableCookie.encode(cookie))
            prefsWriter.apply()
        }
    }

    @Synchronized
    override fun getCookies(url: HttpUrl): MutableList<Cookie> {
        val result = mutableListOf<Cookie>()
        val mapCookie = cookies[url.host]
        if (mapCookie != null) result.addAll(mapCookie.values)
        return result
    }

    @Synchronized
    override fun getAllCookie(): MutableList<Cookie> {
        val result = mutableListOf<Cookie>()
        cookies.forEach {
            result.addAll(it.value.values)
        }
        return result
    }

    @Synchronized
    override fun removeCookie(url: HttpUrl, cookie: Cookie) {
        if (!cookies.containsKey(url.host)) return
        val cookieToken = getCookieToken(cookie)
        val cookieMap = cookies[url.host]
        if (cookieMap != null && !cookieMap.containsKey(cookieToken)) return

        //内存移除
        cookieMap?.remove(cookieToken)
        //文件移除
        val prefsWriter = cookiePrefs.edit()
        if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)) {
            prefsWriter.remove(COOKIE_NAME_PREFIX + cookieToken)
        }
        // 更新索引
        prefsWriter.putString(url.host, formatCookieTokens(url.host))
        prefsWriter.apply()
    }

    @Synchronized
    override fun removeCookies(url: HttpUrl) {
        if (!cookies.containsKey(url.host)) return

        //内存移除
        val urlCookies = cookies.remove(url.host) ?: return
        //文件移除
        val cookieTokens = urlCookies.keys
        val prefsWriter = cookiePrefs.edit()
        for (cookieToken in cookieTokens) {
            if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + cookieToken)
            }
        }
        // 更新索引
        prefsWriter.remove(url.host)
        prefsWriter.apply()
    }

    @Synchronized
    override fun clear() {
        //内存移除
        cookies.clear()
        //文件移除
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()
    }

    private fun getCookieToken(cookie: Cookie): String {
        return cookie.name + "@" + cookie.domain
    }

    private fun formatCookieTokens(host: String): String {
        val iterator = cookies[host]?.keys?.iterator()
        if (iterator== null || !iterator.hasNext()) {
            return ""
        }
        val sb = StringBuilder()
        sb.append(iterator.next())
        while (iterator.hasNext()) {
            sb.append(",")
            sb.append(iterator.next())
        }
        return sb.toString()
    }
}