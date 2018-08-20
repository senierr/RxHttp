package com.senierr.sehttp.cache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import okhttp3.internal.Util;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

/**
 * DiskLruCache缓存处理
 *
 * @author zhouchunjie
 * @date 2018/8/18
 */
public class DiskLruCacheStore implements CacheStore {

    private static final int APP_VERSION = 20170106; /* Son's birthday */
    private static final int VALUE_COUNT = 1;
    private static final int ENTRY_METADATA = 0;

    private DiskLruCache diskLruCache;

    public DiskLruCacheStore(File directory, long maxSize) {
        diskLruCache = DiskLruCache.create(FileSystem.SYSTEM, directory, APP_VERSION, VALUE_COUNT, maxSize);
    }

    private String encodeKey(String key) {
        return ByteString.encodeUtf8(key).md5().hex();
    }

    @Override
    public void put(String key, Serializable value) {
        DiskLruCache.Editor editor = null;
        ObjectOutputStream oos = null;
        try {
            editor = diskLruCache.edit(encodeKey(key));
            if (editor != null) {
                BufferedSink sink = Okio.buffer(editor.newSink(ENTRY_METADATA));
                oos = new ObjectOutputStream(sink.outputStream());
                oos.writeObject(value);
                oos.flush();
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
            abortQuietly(editor);
        } finally {
            Util.closeQuietly(oos);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        T result = null;
        DiskLruCache.Snapshot snapshot = null;
        ObjectInputStream ois = null;
        try {
            snapshot = diskLruCache.get(encodeKey(key));
            if (snapshot != null) {
                BufferedSource source = Okio.buffer(snapshot.getSource(ENTRY_METADATA));
                ois = new ObjectInputStream(source.inputStream());
                result = (T) ois.readObject();
                snapshot.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(ois);
            Util.closeQuietly(snapshot);
        }
        return result;
    }

    private void abortQuietly(DiskLruCache.Editor editor) {
        try {
            if (editor != null) {
                editor.abort();
            }
        } catch (IOException ignored) {
        }
    }
}