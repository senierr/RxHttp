package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.FileConverter;
import com.senierr.sehttp.convert.FileConverter1;

import java.io.File;

import okhttp3.Response;

/**
 * 文件下载回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public abstract class FileCallback1 extends BaseCallback<File> {

    private File destDir;
    private String destName;
    private long startPoint;

    public FileCallback1(File destDir, String destName, long startPoint) {
        this.destDir = destDir;
        this.destName = destName;
        this.startPoint = startPoint;
    }

    /**
     * 判断是否是同一文件
     *
     * 异步线程
     *
     * @return true: 相同文件
     *         false: 不是相同文件
     */
    public boolean onDiff(Response response, File destFile) {
        return false;
    }

    @Override
    public File convert(Response response) throws Exception {
        return new FileConverter1(this, startPoint).convert(response);
    }

    public File getDestDir() {
        return destDir;
    }

    public FileCallback1 setDestDir(File destDir) {
        this.destDir = destDir;
        return this;
    }

    public String getDestName() {
        return destName;
    }

    public FileCallback1 setDestName(String destName) {
        this.destName = destName;
        return this;
    }
}
