package com.senierr.sehttp.callback;

import java.io.File;
import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * 文件下载回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class FileCallback extends BaseCallback<File> {

    private File destDir;
    private String destName;

    public FileCallback(File destDir, String destName) {
        this.destDir = destDir;
        this.destName = destName;
    }

    @Override
    public File convert(Response response) throws Exception {
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
