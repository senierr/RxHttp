# SeHttp

[![](https://jitpack.io/v/senierr/SeHttp.svg)](https://jitpack.io/#senierr/SeHttp)
[![](https://img.shields.io/travis/rust-lang/rust.svg)](https://github.com/senierr/SeHttp)
[![](https://img.shields.io/badge/dependencies-okhttp-green.svg)](https://github.com/square/okhttp)
[![](https://img.shields.io/badge/dependencies-okio-green.svg)](https://github.com/square/okio)

> 专注于网络请求的高效框架，底层基于`okhttp3`；  
> 此框架不参与任何数据持久化，`缓存`及`Cookie`管理，请参考DiskLruCache工具：[SeCache](https://github.com/senierr/SeCache)

## 目前支持
* 普通get, post, put, delete, head, options, patch请求
* 自定义请求参数，请求头，请求体
* 文件下载、上传
* 301、302重定向
* 多种HTTPS验证
* 自定义失败重连次数
* 链式调用
* 根据Tag取消请求
* 可扩展Callback

## 基本用法

#### 1. 导入仓库：

```java
maven { url 'https://jitpack.io' }
```

#### 2. 添加依赖

```java
compile 'com.github.senierr:SeHttp:lastest.version'
```

#### 3. 添加权限

```java
<uses-permission android:name="android.permission.INTERNET"/>
// 文件下载需要以下权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 全局配置（非必须）

```java
SeHttp.getInstance()
        .debug("SeHttp", LogLevel.BODY)               // 开启调试
        .connectTimeout(SeHttp.DEFAULT_TIMEOUT)       // 设置超时，默认30秒
        .readTimeout(SeHttp.DEFAULT_TIMEOUT)
        .writeTimeout(SeHttp.DEFAULT_TIMEOUT)
        .addInterceptor()                             // 添加应用层拦截器
        .addNetworkInterceptor()                      // 添加网络层拦截器
        .hostnameVerifier()                           // 设置域名匹配规则
        .cookieJar()                                  // 设置自定义cookie管理
        .sslSocketFactory()                           // 设置SSL认证
        .addCommonHeader("comHeader", "comValue")     // 添加全局头
        .addCommonUrlParam("comKey", "comValue")      // 添加全局参数
        .retryCount(3);                               // 设置失败重连次数，默认不重连（0次）
```

### GET请求

```java
SeHttp.get(urlStr)
        .addUrlParam("key", "value")                  // 添加单个URL参数
        .addHeader("header", "value")                 // 添加单个请求头
        .execute(new StringCallback() {               // 异步请求
            ...
        });
```

### POST请求

```java
SeHttp.post(urlStr)
        .requestBody4Text()                           // 设置文本
        .requestBody4JSon(jsonObject.toString())      // 设置JSON
        .requestBody4Xml()                            // 设置XML
        .requestBody4Byte()                           // 设置字节流
        .addRequestParam("key", "param")              // 添加表单键值对
        .execute(new StringCallback() {               // 异步请求
            ...
        });
```

### 文件上传

```java
SeHttp.post(urlStr)
        .addRequestParam("key", new File())           // 添加文件
        .execute(new FileCallback(...) {               // 异步请求
            ...
        });
```

### 文件下载

```java
SeHttp.get(Urls.URL_DOWNLOAD)
        .execute(new FileCallback(...) {
            @Override
            public boolean onDiff(Response response, File destFile) {
                // 判断destFile是否是需要下载的文件，默认返回false
                return super.onDiff(response, destFile);
            }
            ......
        });
```

## 请求回调

```java
/**
 * 请求开始前回调，可通过requestBuilder修改请求
 *
 * @param requestBuilder 请求构造器
 */
public void onBefore(RequestBuilder requestBuilder) {}

/**
 * 上传进度回调
 *
 * @param totalSize 上传文件总大小
 * @param currentSize 当前已上传大小
 * @param progress 进度0~100
 */
public void onUploadProgress(long totalSize, long currentSize, int progress) {}

/**
 * 下载进度回调
 *
 * @param totalSize 下载文件总大小
 * @param currentSize 当前已下载大小
 * @param progress 进度0~100
 */
public void onDownloadProgress(long totalSize, long currentSize, int progress) {}

/**
 * 请求成功回调
 *
 * @param t 泛型
 */
public abstract void onSuccess(T t);

/**
 * 请求异常回调
 *
 * @param isCanceled 是否取消
 * @param e 捕获的异常
 */
public void onError(boolean isCanceled, Exception e) {}

/**
 * 请求结束回调
 */
public void onAfter() {}
```

## 取消请求

```java
// 取消对应tag请求
SeHttp.getInstance().cancelTag(tag);
// 取消所有请求
SeHttp.getInstance().cancelAll();
```

## 注意事项

`SeHttp`底层基于`okhttp3`，所以默认依赖：

```java
compile 'com.squareup.okhttp3:okhttp:3.8.1'
compile 'com.squareup.okio:okio:1.13.0'
```

## 混淆

```java
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
```
