# SeHttp

`SeHttp`是基于`okhttp3`封装的网络请求框架，用于简化网络请求。

[![](https://jitpack.io/v/senierr/sehttp.svg)](https://jitpack.io/#senierr/sehttp)

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
* 支持多种缓存模式
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
compile 'com.github.senierr:sehttp:RELEASE_VERSION'

或者

compile 'com.github.senierr:sehttp:+'        //版本号使用 + 可以自动引用最新版
```

#### 3. 添加权限

```java
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
### 全局配置/初始化

**初始化必须执行, 初始化必须执行, 初始化必须执行**

```java
SeHttp.init(getApplication())
        .debug("SeHttp")                              // 开启调试
        .debug(tag, isLogException)
        .connectTimeout(SeHttp.DEFAULT_TIMEOUT)       // 设置超时，默认30秒
        .readTimeout(SeHttp.DEFAULT_TIMEOUT)
        .writeTimeout(SeHttp.DEFAULT_TIMEOUT)
        .addInterceptor()                             // 添加应用层拦截器
        .addNetworkInterceptor()                      // 添加网络层拦截器
        .hostnameVerifier()                           // 设置域名匹配规则
        .addCommonHeader("comHeader", "comValue")     // 添加全局头
        .addCommonHeaders()
        .addCommonUrlParam("comKey", "comValue")      // 添加全局参数
        .addCommonUrlParams()
        .cacheConfig(cacheConfig)                     // 设置缓存参数
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
        .cacheKey(urlStr)                             // 设置缓存key
        .cacheMode(CacheMode.CACHE_FAILED_REQUEST)    // 设置缓存模式，默认NO_CACHE
        .cacheTime(1000 * 10)                         // 设置缓存有效时长
        .execute()                                    // 同步请求
        .execute(new StringCallback() {               // 异步请求
            ......
        });
```

### 文件下载

判断文件是否已下载，请重写`onDiff()`方法实现，默认返回false;

```java
SeHttp.get(Urls.URL_DOWNLOAD)
        .execute(new FileCallback(path + "SeHttp.txt") {
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
 * 文件上传进度回调
 *
 * @param currentSize 当前已上传大小
 * @param totalSize 上传文件总大小
 * @param progress 进度0~100
 * @param networkSpeed 网速
 */
public void uploadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

/**
 * 线程：UI线程
 *
 * 文件下载进度回调
 *
 * @param currentSize 当前已下载大小
 * @param totalSize 下载文件总大小
 * @param progress 进度0~100
 * @param networkSpeed 网速
 */
public void downloadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

/**
 * 线程：UI线程
 *
 * 请求成功回调
 *
 * @param t 泛型
 * @param isCache 是否是缓存数据
 * @throws Exception 异常抛出，onError()会捕获
 */
public abstract void onSuccess(T t, boolean isCache) throws Exception;

/**
 * 线程：UI线程
 *
 * 请求异常回调
 *
 * @param isCanceled 请求是否主动终止
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

## 缓存管理

SeHttp的缓存是通过异步的`DiskLruCache`来实现的；

所以，缓存的泛型对象T必须实现`Serializable`接口，例如`String`类型；

#### 缓存配置：

```java
CacheConfig cacheConfig = CacheConfig.build()
                .cacheFile(FileUtil.getCacheDirectory(this, null))          // 设置缓存路径，默认在应用缓存目录
                .cacheTime(1000 * 3600 * 24 * 7)                            // 设置缓存有效时长
                .maxSize(1024 * 1024 * 10);                                 // 设置缓存大小

SeHttp.init(getApplication()).cacheConfig(cacheConfig);
或者
SeHttp.getInstance().cacheConfig(cacheConfig);
```

#### 缓存类型`CacheMode`及对应的回调生命周期

**NO_CACHE // 无缓存模式**

>onBefore() -> onSuccess(t, false)/onError() -> onAfter()

**REQUEST_FAILED_CACHE // 先请求网络，成功则使用网络，失败读取缓存**

>onBefore() -> onSuccess(t, false/true)/onError() -> onAfter()

**CACHE_FAILED_REQUEST // 先读取缓存，成功则使用缓存，失败请求网络**

>onBefore() -> onSuccess(t, true/false)/onError() -> onAfter()

**CACHE_THEN_REQUEST // 先读取缓存，无论成功与否，然后请求网络**

>有缓存：onBefore() -> onSuccess(t, true) -> onAfter() -> onSuccess(t, false)/onError() -> onAfter()  
>没缓存：onBefore() -> onSuccess(t, false)/onError() -> onAfter()

#### 使用方式

```java
SeHttp.get(urlStr)
        .cacheKey(key)
        .cacheMode(CacheMode.NO_CACHE)
        .cacheTime(1000 * 60);
```
#### 缓存数据回调

```java
@Override
public void onSuccess(String s, boolean isCache) throws Exception {
    // isCache: 判断是否是缓存数据
}
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
