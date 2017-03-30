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

    private File destFile;

    public FileCallback(File destFile) {
        this.destFile = destFile;
    }

    public FileCallback(String destFilePath) {
        this.destFile = new File(destFilePath);
    }

    /**
     * 判断是否是同一文件
     *
     * 默认为否，删除重新下载
     *
     * 返回是，则直接回调Success
     *
     * @return
     */
    public boolean isDiff() {
        return false;
    }

    @Override
    public File convert(Response response) throws Exception {
        return new FileConverter(this).convert(response);
    }

    public File getDestFile() {
        return destFile;
    }

    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }
}
