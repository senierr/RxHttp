package com.senierr.sehttp.convert;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * File类型解析
 *
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
        File destFileDir = new File(destFile.getParent() + File.separator);
        if (!destFileDir.exists()) {
            destFileDir.mkdirs();
        }
        if (destFile.exists()) {
            if (fileCallback.onDiff(response, destFile)) {
                return destFile;
            } else {
                destFile.delete();
            }
        }

        // 上次刷新的时间
        long lastRefreshUiTime = 0;

        InputStream is = null;
        byte[] bytes = new byte[512];
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            long sum = 0;
            int len;
            fos = new FileOutputStream(destFile);
            while ((len = is.read(bytes)) != -1) {
                sum += len;
                fos.write(bytes, 0, len);

                final long finalSum = sum;
                long curTime = System.currentTimeMillis();
                if (curTime - lastRefreshUiTime >= SeHttp.REFRESH_MIN_INTERVAL || finalSum == total) {
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (fileCallback != null) {
                                fileCallback.onProgress(total, finalSum, (int) (finalSum * 100 / total));
                            }
                        }
                    });

                    lastRefreshUiTime = System.currentTimeMillis();
                }
            }
            fos.flush();
            return destFile;
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
