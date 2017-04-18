package com.senierr.sehttp.convert;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.util.SeLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Response;

/**
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class FileConverter implements Converter<File> {

    private FileCallback fileCallback;
    private File destFile;

    public FileConverter(FileCallback fileCallback) {
        this.fileCallback = fileCallback;
        destFile = fileCallback.getDestFile();
    }

    @Override
    public File convert(Response response) throws Exception {
        int responseCode = response.code();
        if (!response.isSuccessful()) {
            throw new Exception("Response is not successful! responseCode: " + responseCode);
        }

        File destFileDir = new File(destFile.getParent() + File.separator);
        if (!destFileDir.exists()) {
            destFileDir.mkdirs();
        }
        if (destFile.exists()) {
            if (fileCallback.isDiff(response, destFile)) {
                return destFile;
            } else {
                destFile.delete();
            }
        }

        // 生成非重复的临时文件
        int index = 1;
        File tempFile = new File(destFile.getAbsolutePath() + "_temp_" + System.currentTimeMillis());
        // 临时文件存在,则+1
        while (index < 10 && tempFile.exists()) {
            tempFile = new File(destFile.getAbsolutePath() + "_temp_" + System.currentTimeMillis() + "_" + index);
            index++;
        }
        // 若还存在
        if (tempFile.exists()) {
            throw new Exception("TempFile create failure!");
        }

        // 上次刷新的时间
        long lastRefreshUiTime = 0;
        // 上次写入字节数据
        long lastWriteBytes = 0;

        InputStream is = null;
        byte[] buf = new byte[512];
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
                if (curTime - lastRefreshUiTime >= SeHttp.REFRESH_MIN_INTERVAL || finalSum == total) {
                    //计算下载速度
                    long diffTime = (curTime - lastRefreshUiTime) / 1000;
                    if (diffTime == 0) diffTime += 1;
                    long diffBytes = finalSum - lastWriteBytes;
                    final long networkSpeed = diffBytes / diffTime;
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            fileCallback.downloadProgress(finalSum, total, (int) (finalSum * 100 / total), networkSpeed);
                        }
                    });

                    lastRefreshUiTime = System.currentTimeMillis();
                    lastWriteBytes = finalSum;
                }
            }
            fos.flush();
            if (destFile.exists()) {
                destFile.delete();
            }
            if (!tempFile.renameTo(destFile)) {
                SeLogger.e(tempFile.getName() + " rename to " + destFile.getName() + " failure!");
                return tempFile;
            }
        } finally {
            if (is != null) is.close();
            if (fos != null) fos.close();
        }
        return destFile;
    }
}
