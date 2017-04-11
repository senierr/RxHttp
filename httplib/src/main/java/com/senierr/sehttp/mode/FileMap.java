package com.senierr.sehttp.mode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求体参数
 *
 * @author zhouchunjie
 * @date 2017/3/30
 */

public class FileMap {

    private List<String> keyList;
    private List<File> fileList;

    public FileMap() {
        keyList = new ArrayList<>();
        fileList = new ArrayList<>();
    }

    public boolean isEmpty() {
        return keyList.isEmpty() || fileList.isEmpty();
    }

    public int size() {
        return keyList.size() < fileList.size() ? keyList.size() : fileList.size();
    }

    public void add(String key, File file) {
        keyList.add(key);
        fileList.add(file);
    }

    public void addAll(FileMap fileMap) {
        if (fileMap == null) {
            return;
        }
        for (int i = 0; i < fileMap.size(); i++) {
            keyList.add(fileMap.keyAt(i));
            fileList.add(fileMap.fileAt(i));
        }
    }

    public String keyAt(int position) {
        return keyList.get(position);
    }

    public File fileAt(int position) {
        return fileList.get(position);
    }

    public void remove(int position) {
        keyList.remove(position);
        fileList.remove(position);
    }

    public void clear() {
        keyList.clear();
        fileList.clear();
    }
}
