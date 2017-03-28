package com.senierr.sehttp.callback;

import android.os.Handler;

import com.senierr.sehttp.SeHttp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Response;

/**
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class FileCallback extends BaseCallback<File> {

    private File destFile;

    public FileCallback(File destFile) {
        this.destFile = destFile;
    }

    public void onProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

    @Override
    public void convert(final Response response, Handler mainScheduler) throws Exception {
        final int responseCode = response.code();
        if (responseCode != 200) {
            sendError(mainScheduler, responseCode, null);
        } else {
            if (destFile.exists()) {
                destFile.delete();
            }
            File tempFile = new File(destFile.getAbsolutePath() + "_temp");

            // 上次刷新的时间
            long lastRefreshUiTime = 0;
            // 上次写入字节数据
            long lastWriteBytes = 0;

            InputStream is = null;
            byte[] buf = new byte[1024];
            FileOutputStream fos = null;
            try {
                is = response.body().byteStream();
                final long total = response.body().contentLength();
                long sum = 0;
                int len;
                fos = new FileOutputStream(tempFile);
                while ((len = is.read(buf)) != -1) {
                    sum += len;
                    fos.write(buf, 0, len);

                    final long finalSum = sum;
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastRefreshUiTime >= SeHttp.REFRESH_INTERVAL || finalSum == total) {
                        //计算下载速度
                        long diffTime = (curTime - lastRefreshUiTime) / 1000;
                        if (diffTime == 0) diffTime += 1;
                        long diffBytes = finalSum - lastWriteBytes;
                        final long networkSpeed = diffBytes / diffTime;
                        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                            @Override
                            public void run() {
                                onProgress(finalSum, total, (int) (finalSum * 100 / total), networkSpeed);
                            }
                        });

                        lastRefreshUiTime = System.currentTimeMillis();
                        lastWriteBytes = finalSum;
                    }
                }
                fos.flush();
                response.close();
                if (!tempFile.renameTo(destFile)) {
                    sendError(mainScheduler, -1, new Exception("File rename error!"));
                }
                mainScheduler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onSuccess(destFile);
                        } catch (Exception e) {
                            onError(-1, e);
                        } finally {
                            onAfter();
                        }
                    }
                });
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
            }
        }
    }

    /**
     * 执行失败结束回调
     *
     * @param mainScheduler
     * @param responseCode
     * @param e
     */
    private void sendError(Handler mainScheduler, final int responseCode, final Exception e) {
        mainScheduler.post(new Runnable() {
            @Override
            public void run() {
                onError(responseCode, e);
                onAfter();
            }
        });
    }
}
