package com.senierr.sehttp.converter;

import java.io.File;
import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * File转换器
 *
 * @author zhouchunjie
 * @date 2018/5/17
 */
public class FileConverter implements Converter<File> {

    private File destDir;
    private String destName;

    public FileConverter(File destFile) {
        this.destDir = destFile.getParentFile();
        this.destName = destFile.getName();
    }

    public FileConverter(File destDir, String destName) {
        this.destDir = destDir;
        this.destName = destName;
    }

    @Override
    public File onConvert(Response response) throws Exception {
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
                throw new IOException("ResponseBody is null");
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
