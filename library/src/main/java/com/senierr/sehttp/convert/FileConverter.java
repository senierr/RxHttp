package com.senierr.sehttp.convert;

import com.senierr.sehttp.callback.FileCallback;

import java.io.File;
import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * File类型解析
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class FileConverter implements Converter<File> {

    private FileCallback fileCallback;

    public FileConverter(FileCallback fileCallback) {
        this.fileCallback = fileCallback;
    }

    @Override
    public File convert(Response response) throws Exception {
        File destDir = fileCallback.getDestDir();
        String destName = fileCallback.getDestName();

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
            if (fileCallback.onDiff(response, destFile)) {
                return destFile;
            } else {
                boolean result = destFile.delete();
                if (!result) {
                    throw new Exception(destFile.getPath() + " delete failed!");
                }
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
