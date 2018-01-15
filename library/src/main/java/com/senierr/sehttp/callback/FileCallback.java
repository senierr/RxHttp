package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.FileConverter;

import java.io.File;

import okhttp3.Response;

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

    /**
     * 判断是否是同一文件
     *
     * 注：异步线程
     *
     * @return true: 相同文件
     *         false: 不是相同文件
     */
    public boolean onDiff(Response response, File destFile) {
        return false;
    }

    @Override
    public File convert(Response response) throws Exception {
        return new FileConverter(this).convert(response);
    }

    public File getDestDir() {
        return destDir;
    }

    public FileCallback setDestDir(File destDir) {
        this.destDir = destDir;
        return this;
    }

    public String getDestName() {
        return destName;
    }

    public FileCallback setDestName(String destName) {
        this.destName = destName;
        return this;
    }
}
