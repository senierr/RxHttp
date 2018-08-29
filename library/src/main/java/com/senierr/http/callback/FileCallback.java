package com.senierr.http.callback;

import com.senierr.http.converter.FileConverter;

import java.io.File;

/**
 * 文件下载回调
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public abstract class FileCallback extends Callback<File> {

    public FileCallback(File destFile) {
        this(destFile.getParentFile(), destFile.getName());
    }

    public FileCallback(File destDir, String destName) {
        super(new FileConverter(destDir, destName));
    }
}
