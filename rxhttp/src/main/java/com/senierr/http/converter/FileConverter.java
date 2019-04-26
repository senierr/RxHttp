package com.senierr.http.converter;

import java.io.File;
import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * 文件下载转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public class FileConverter implements Converter<File> {

    private @NonNull File destDir;
    private @NonNull String destName;

    public FileConverter(@NonNull File destFile) {
        this.destDir = destFile.getParentFile();
        this.destName = destFile.getName();
    }

    public FileConverter(@NonNull File destDir, @NonNull String destName) {
        this.destDir = destDir;
        this.destName = destName;
    }

    public @NonNull File convertResponse(@NonNull Response response) throws Throwable {
        // 判断路径是否存在
        if (!destDir.exists()) {
            boolean result = destDir.mkdirs();
            if (!result) {
                throw new Exception(destDir.getPath() + " create failed!");
            }
        }

        File destFile = new File(destDir, destName);
        // 判断文件是否存在
        if (destFile.exists()) {
            boolean result = destFile.delete();
            if (!result) {
                throw new Exception(destFile.getPath() + " delete failed!");
            }
        }

        BufferedSource bufferedSource = null;
        BufferedSink bufferedSink = null;
        try {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("ResponseBody is null!");
            }

            bufferedSource = Okio.buffer(Okio.source(responseBody.byteStream()));
            bufferedSink = Okio.buffer(Okio.sink(destFile));

            byte[] bytes = new byte[1024];
            int len;
            while ((len = bufferedSource.read(bytes)) != -1) {
                bufferedSink.write(bytes, 0, len);
            }
            bufferedSink.flush();
            return destFile;
        } finally {
            Util.closeQuietly(bufferedSource);
            Util.closeQuietly(bufferedSink);
        }
    }
}
