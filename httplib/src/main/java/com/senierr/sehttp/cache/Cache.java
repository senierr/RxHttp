package com.senierr.sehttp.cache;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.util.EncryptUtils;
import com.senierr.sehttp.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author zhouchunjie
 * @date 2017/4/19
 */

public class Cache {

    /**
     * 写入缓存
     *
     */
    public static void writeCache(CacheEntity cacheEntity) {
        CacheConfig cacheConfig = SeHttp.getInstance().getCacheConfig();
        if (cacheConfig == null) {
            return;
        }
        if (cacheConfig.getCacheFile() == null) {
            return;
        }
        if (!cacheConfig.getCacheFile().exists()) {
            if (!cacheConfig.getCacheFile().mkdirs()) {
                return;
            }
        }

        File cacheFile = new File(cacheConfig.getCacheFile(),
                EncryptUtils.encryptMD5ToString(cacheEntity.getKey()));
        if (cacheFile.exists()) {
            if (!cacheFile.delete()) {
                return;
            }
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(cacheFile, true));
            bw.write(new String(EncryptUtils.encryptAES(cacheEntity.toJson().getBytes(),
                    EncryptUtils.encryptMD5ToString(cacheEntity.getKey()).getBytes())));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public static CacheEntity readCache(String key) {
        CacheConfig cacheConfig = SeHttp.getInstance().getCacheConfig();
        if (cacheConfig == null) {
            return null;
        }
        if (cacheConfig.getCacheFile() == null) {
            return null;
        }

        File cacheFile = new File(cacheConfig.getCacheFile(), key);
        if (!cacheFile.exists()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            CacheEntity cacheEntity = CacheEntity.parseJson(
                    new String(EncryptUtils.decryptAES(sb.toString().getBytes(), EncryptUtils.encryptMD5ToString(key).getBytes())));
            return cacheEntity;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
