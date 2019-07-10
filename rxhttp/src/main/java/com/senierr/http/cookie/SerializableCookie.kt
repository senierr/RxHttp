package com.senierr.http.cookie

import android.util.Log
import com.senierr.http.util.Utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

import okhttp3.Cookie

/**
 * 可序列化的Cookie
 *
 * @author zhouchunjie
 * @date 2018/8/14
 */
class SerializableCookie(@Transient private var cookie: Cookie? = null) : Serializable {

    private fun writeObject(out: ObjectOutputStream) {
        cookie?.let {
            out.writeObject(it.name())
            out.writeObject(it.value())
            out.writeLong(if (it.persistent()) it.expiresAt() else NON_VALID_EXPIRES_AT)
            out.writeObject(it.domain())
            out.writeObject(it.path())
            out.writeBoolean(it.secure())
            out.writeBoolean(it.httpOnly())
            out.writeBoolean(it.hostOnly())
        }
    }

    private fun readObject(inputStream: ObjectInputStream) {
        val builder = Cookie.Builder()
        builder.name(inputStream.readObject() as String)
        builder.value(inputStream.readObject() as String)
        val expiresAt = inputStream.readLong()
        if (expiresAt != NON_VALID_EXPIRES_AT) {
            builder.expiresAt(expiresAt)
        }

        val domain = inputStream.readObject() as String
        builder.domain(domain)
        builder.path(inputStream.readObject() as String)
        if (inputStream.readBoolean()) builder.secure()
        if (inputStream.readBoolean()) builder.httpOnly()
        if (inputStream.readBoolean()) builder.hostOnlyDomain(domain)
        cookie = builder.build()
    }

    override fun equals(other: Any?): Boolean {
        return other is SerializableCookie && cookie == other.cookie
    }

    override fun hashCode(): Int {
        return cookie.hashCode()
    }

    companion object {

        private val TAG = SerializableCookie::class.java.simpleName
        private const val serialVersionUID = -8594045714036645534L
        private const val NON_VALID_EXPIRES_AT = -1L

        /**
         * Cookie转字符串
         */
        fun encode(cookie: Cookie): String? {
            val byteArrayOutputStream = ByteArrayOutputStream()
            var objectOutputStream: ObjectOutputStream? = null

            try {
                objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
                objectOutputStream.writeObject(SerializableCookie(cookie))
            } catch (e: IOException) {
                Log.d(TAG, "IOException in encodeCookie", e)
                return null
            } finally {
                Utils.closeQuietly(objectOutputStream)
            }
            return byteArrayToHexString(byteArrayOutputStream.toByteArray())
        }

        /**
         * 字符串转Cookie
         */
        fun decode(encodedCookie: String): Cookie? {
            val bytes = hexStringToByteArray(encodedCookie)
            val byteArrayInputStream = ByteArrayInputStream(bytes)

            var cookie: Cookie? = null
            var objectInputStream: ObjectInputStream? = null
            try {
                objectInputStream = ObjectInputStream(byteArrayInputStream)
                cookie = (objectInputStream.readObject() as SerializableCookie).cookie
            } catch (e: IOException) {
                Log.d(TAG, "IOException in decodeCookie", e)
            } catch (e: ClassNotFoundException) {
                Log.d(TAG, "ClassNotFoundException in decodeCookie", e)
            } finally {
                Utils.closeQuietly(objectInputStream)
            }
            return cookie
        }

        /**
         * 二进制数组转十六进制字符串
         *
         * @param bytes
         * @return
         */
        private fun byteArrayToHexString(bytes: ByteArray): String {
            val sb = StringBuilder(bytes.size * 2)
            for (element in bytes) {
                val v = element.toInt() and 0xff
                if (v < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(v))
            }
            return sb.toString()
        }

        /**
         * 十六进制字符串转二进制数组
         *
         * @param hexString
         * @return
         */
        private fun hexStringToByteArray(hexString: String): ByteArray {
            val len = hexString.length
            val data = ByteArray(len / 2)
            for (i in 0 until len step 2) {
                data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
            }
            return data
        }
    }
}