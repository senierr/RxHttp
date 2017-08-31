package com.senierr.sehttp.convert;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;

import java.io.File;
import java.io.IOException;

import okhttp3.Response;
import okhttp3.ResponseBody;
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
            // 计算总大小
            final long total = responseBody.contentLength();
            // 上次刷新的时间
            long lastTime = 0;

            byte[] bytes = new byte[1024];
            long sum = 0;
            int len;
            while ((len = bufferedSource.read(bytes)) != -1) {
                sum += len;
                bufferedSink.write(bytes, 0, len);

                final long finalSum = sum;
                long curTime = System.currentTimeMillis();
                if (curTime - lastTime >= SeHttp.REFRESH_MIN_INTERVAL || finalSum == total) {
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (fileCallback != null) {
                                fileCallback.onDownloadProgress(total, finalSum, (int) (finalSum * 100 / total));
                            }
                        }
                    });
                    lastTime = curTime;
                }
            }
            bufferedSink.flush();
            return destFile;
        } finally {
            try {
                if (bufferedSource != null) bufferedSource.close();
                if (bufferedSink != null) bufferedSink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
