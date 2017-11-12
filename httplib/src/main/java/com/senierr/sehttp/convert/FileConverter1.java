package com.senierr.sehttp.convert;

import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.callback.FileCallback1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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

public class FileConverter1 implements Converter<File> {

    private FileCallback1 fileCallback;
    private long startPoint;

    public FileConverter1(FileCallback1 fileCallback, long startPoint) {
        this.fileCallback = fileCallback;
        this.startPoint = startPoint;
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
//        if (destFile.exists()) {
//            if (fileCallback.onDiff(response, destFile)) {
//                return destFile;
//            } else {
//                boolean result = destFile.delete();
//                if (!result) {
//                    throw new Exception(destFile.getPath() + " delete failed!");
//                }
//            }
//        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("ResponseBody is null");
        }

        InputStream in = responseBody.byteStream();
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(destFile, "rwd");
            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            channelOut = randomAccessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startPoint, responseBody.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
            return destFile;
        } finally {
            Util.closeQuietly(in);
            Util.closeQuietly(channelOut);
            Util.closeQuietly(randomAccessFile);
        }
    }
}
