# SeHttp

> 网络请求框架，底层基于`okhttp3`
>
> 若需使用缓存管理，可以参考[SeCache](https://github.com/senierr/SeCache)

[![](https://jitpack.io/v/senierr/SeHttp.svg)](https://jitpack.io/#senierr/SeHttp)

## 目前支持
* 普通get, post, put, delete, head, options, patch请求
* 自定义请求参数
* 自定义请求头
* 自定义多种请求体
* 多文件和多参数统一的表单上传
* 文件下载和下载进度回调
* 文件上传和上传进度回调
* 301、302重定向
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
compile 'com.github.senierr:SeHttp:RELEASE_VERSION'
```

#### 3. 添加权限

```java
<uses-permission android:name="android.permission.INTERNET"/>
```

### 全局配置/初始化

`非必须`

```java
SeHttp.getInstance()
        .debug("SeHttp")                              // 开启调试
        .connectTimeout(SeHttp.DEFAULT_TIMEOUT)       // 设置超时，默认30秒
        .readTimeout(SeHttp.DEFAULT_TIMEOUT)
        .writeTimeout(SeHttp.DEFAULT_TIMEOUT)
        .addInterceptor()                             // 添加应用层拦截器
        .addNetworkInterceptor()                      // 添加网络层拦截器
        .hostnameVerifier()                           // 设置域名匹配规则
        .cookieJar()                                  // 设置自定义cookie管理
        .sslSocketFactory()                           // 设置SSL认证
        .addCommonHeader("comHeader", "comValue")     // 添加全局头
        .addCommonHeaders()
        .addCommonUrlParam("comKey", "comValue")      // 添加全局参数
        .addCommonUrlParams()
        .retryCount(3);                               // 设置请求失败重连次数，默认不重连（0次）
```

### 基本请求

```java
SeHttp.get(urlStr)                                    // 请求方式及URL
        .tag(this)                                    // 设置标签，用于取消请求
        .addUrlParam("key", "value")                  // 添加单个URL参数
        .addUrlParams()                               // 添加多个URL参数
        .addHeader("header", "value")                 // 添加单个请求头
        .addHeaders()                                 // 添加多个请求头
        .requestBody4Text()                           // 设置文本格式请求体
        .requestBody4JSon(jsonObject.toString())      // 设置JSON格式请求体
        .requestBody4Xml()                            // 设置XML格式请求体
        .requestBody4Byte()                           // 设置字节流格式请求提
        .requestBody()                                // 设置自定义请求体
        .addRequestParam("key", "param")              // 添加单个请求体键值对（字符串）
        .addRequestParam("key", new File())           // 添加单个请求体键值对（文件）
        .addRequestStringParams()                     // 添加多个请求体键值对（字符串）
        .addRequestFileParams()                       // 添加多个请求体键值对（文件）
        .build()                                      // 生成OkHttp请求
        .execute()                                    // 同步请求
        .execute(new BaseCallback() {               // 异步请求
            ......
        });
```

### 文件下载

```java
SeHttp.get(Urls.URL_DOWNLOAD)
        .execute(new FileCallback(path) {
            @Override
            public boolean onDiff(Response response, File destFile) {
                // 判断destFile是否是需要下载的文件，默认返回false
                return super.onDiff(response, destFile);
            }
            ......
        });
```

### 请求回调

```java
/**
 * 线程：UI线程
 *
 * 请求开始前回调，可通过requestBuilder修改请求
 *
 * @param requestBuilder 请求构造器
 */
public void onBefore(RequestBuilder requestBuilder) {}

/**
 * 线程：UI线程
 *
 * 文件上传下载进度回调
 *
 * @param totalSize 上传下载文件总大小
 * @param currentSize 当前已上传下载大小
 * @param progress 进度0~100
 */
public void onProgress(long totalSize, long currentSize, int progress) {}

/**
 * 线程：UI线程
 *
 * 请求成功回调
 *
 * @param t 泛型
 */
public abstract void onSuccess(T t);

/**
 * 线程：UI线程
 *
 * 请求异常回调
 *
 * @param e 捕获的异常
 */
public void onError(Exception e) {}

/**
 * 线程：UI线程
 *
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

`SeHttp`是基于`okhttp3`所扩展的网络请求框架，所以默认依赖:

```java
compile 'com.squareup.okhttp3:okhttp:3.6.0'
compile 'com.squareup.okio:okio:1.11.0'
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
