# SeHttp

[![](https://jitpack.io/v/senierr/SeHttp.svg)](https://jitpack.io/#senierr/SeHttp)
[![](https://img.shields.io/travis/rust-lang/rust.svg)](https://github.com/senierr/SeHttp)
[![](https://img.shields.io/badge/dependencies-okhttp-green.svg)](https://github.com/square/okhttp)
[![](https://img.shields.io/badge/dependencies-okio-green.svg)](https://github.com/square/okio)

#### 精简、高效的网络请求框架

## 目前支持
* 普通get, post, put, delete, head, options, patch请求
* 自定义公共请求参数、请求头
* 自定义请求参数、请求头、请求体
* 文件下载、上传
* 多级别日志打印
* 301、302重定向
* 多种HTTPS验证
* Cookie持久化管理
* 多种缓存策略
* 自定义失败重连次数
* 根据Tag取消请求
* 链式调用
* 可扩展回调

## 前言

#### 为什么取消单例模式？

假设这么一种场景：
> 有两个业务模块：模块1和模块2，需要设置不同的的SSL加密方式、公共参数或者其他配置。

旧版（1.X.X）由于是单例模式，只能设置一种公共配置，无法为各模块设置不同配置。  
新版（2.X.X）的将单例模式设置上浮至使用者，使其更灵活适应不同的业务模块。


## 1. 配置

#### 1.1. 导入仓库：

```java
maven { url 'https://jitpack.io' }
```

#### 1.2. 添加依赖

```java
implementation 'com.github.senierr:SeHttp:<release_version>'
```

`SeHttp`底层基于`okhttp3`，所以默认依赖：

```java
implementation 'com.squareup.okhttp3:okhttp:3.9.1'
```

#### 1.3. 添加权限

```java
<uses-permission android:name="android.permission.INTERNET"/>
// 文件上传下载需要以下权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 2. 实例化

```java
SeHttp seHttp = new SeHttp.Builder().build();
```
``Builder``可选方法：
```java
-- addCommonUrlParam       // 增加单个公共URL参数
-- addCommonUrlParams      // 增加多个公共URL参数
-- addCommonHeader         // 增加单个公共头信息
-- addCommonHeaders        // 增加多个公共头信息
-- retryCount              // 设置失败重连次数
-- debug                   // 设置Debug模式
-- connectTimeout          // 设置连接超时(ms)
-- readTimeout             // 设置读超时(ms)
-- writeTimeout            // 设置写超时(ms)
-- hostnameVerifier        // 设置域名校验规则
-- sslFactory              // 设置SSL验证
-- cookieJar               // 设置Cookie管理
-- dispatcher              // 设置线程调度器
-- cacheStore              // 设置缓存配置
-- addInterceptor          // 增加拦截器
-- addNetworkInterceptor   // 增加网络层拦截器
```

## 3. 请求

```java
// 创建请求器
RequestFactory requestFactory = new RequestFactory(seHttp, method, urlStr);
// 通过请求器发起请求
requestFactory.execute(...);
```
``RequestFactory``可选方法：
```java
-- tag                      // 设置请求标签
-- addUrlParam              // 增加单个URL参数
-- addUrlParams             // 增加多个URL参数
-- addHeader                // 增加单个头信息
-- addHeaders               // 增加多个头信息
-- addRequestParam          // 增加单个请求体参数
-- addRequestFileParams     // 增加多个文件请求体参数
-- addRequestStringParams   // 增加多个字符串请求体参数
-- setRequestBody4JSon      // 设置Json请求体
-- setRequestBody4Text      // 设置Text请求体
-- setRequestBody4Xml       // 设置XML请求体
-- setRequestBody4Byte      // 设置Byte请求体
-- setRequestBody           // 设置自定义请求体
-- cacheKey                 // 设置缓存Key
-- cachePolicy              // 设置缓存策略
-- cacheDuration            // 设置缓存有效时长
-- buildRequest             // 构造Request
-- execute()                // 同步请求
-- execute(...)             // 异步请求
```

##### 当然，我们也可以用更简洁的链式请求：
```java
seHttp.post(URL_GET)
        .tag(this)
        .addRequestParam("key", "value")
        ...
        .execute(...);
```

## 4. 请求回调

``SeHttp``基础回调``BaseCallback``如下：
```java
// Reponse解析：异步线程
public abstract T convert(Response response) throws Exception;

// 上传监听：主线程
public void onUpload(int progress, long currentSize, long totalSize) {}

// 下载监听：主线程
public void onDownload(int progress, long currentSize, long totalSize) {}

// 缓存成功回调：主线程
public void onCacheSuccess(T t) {}

// 成功回调：主线程
public abstract void onSuccess(T t);

// 失败回调：主线程
public void onFailure(Exception e) {}
```
同时，还提供了扩展的``StringCallback``、``JsonCallback``、``FileCallback``。

#### 4.1. 自定义回调

> ``BaseCallback``中，``convert()``的作用是将``Reponse``解析成需要返回的数据。
> ``SeHttp``内部提供的``StringCallback``、``JsonCallback``、``FileCallback``就是继承``BaseCallback``通过重写``convert()``，实现不同的功能。

了解其原理，那么现在，你也就能通过重写``convert()``自定义适合自己项目的``Callback``了

#### 4.2. 特殊回调

``JsonCallback``扩展了其特有的回调：
```java
// 解析Json数据：异步线程
public abstract T parseJson(String responseStr) throws Exception;
```

#### 4.3. 字符集

对于``StringCallback``、``JsonCallback``，若不设置特殊字符集，默认返回UTF-8类型的字符串。若要设置返回字符集，可通过构造器传入：
```java
public StringCallback(Charset charset)
public JsonCallback(Charset charset)
```

## 5. Cookie

``SeHttp``默认提供以下方式管理``Cookie``：
```java
-- SPCookieJar      // SharedPreferences
```
#### 5.1. 配置

```java
// 实例化时配置Cookie管理
new SeHttp.Builder()
        .cookieJar(new SPCookieJar(this))
        .build();
```

#### 5.2. 手动管理

```java
ClearableCookieJar cookieJar = seHttp.getCookieJar();   // 获取管理器

cookieJar.saveCookie(httpUrl, cookie);      // 保存单个URL对应Cookie
cookieJar.saveCookies(httpUrl, cookies);    // 保存多个URL对应Cookie
cookieJar.getAllCookie();                   // 获取所有Cookie
cookieJar.getCookies(httpUrl);              // 获取URL对应所有Cookie
cookieJar.removeCookie(httpUrl, cookie);    // 移除单个URL对应Cookie
cookieJar.removeCookies(httpUrl);           // 移除URL对应所有Cookie
cookieJar.removeAllCookie();                // 移除所有Cookie
```

#### 5.3. 自定义管理

通过继承``ClearableCookieJar``抽象类，并在实例化时设置给``SeHttp``，实现自己的Cookie管理方式。

## 6. HTTPS

```java
// 实例化时设置SSL验证
new SeHttp.Builder()
        .sslFactory(new SSLFactory())
        .build();

// 默认信任所有证书
public SSLFactory()
// 单向认证
public SSLFactory(X509TrustManager trustManager)
// 单向认证
public SSLFactory(InputStream... certificates)
// 双向认证
public SSLFactory(InputStream bksFile, String password, InputStream... certificates)
// 双向认证
public SSLFactory(InputStream bksFile, String password, X509TrustManager trustManager)
```

## 7. 缓存

``SeHttp``默认提供以下方式管理缓存：
```java
-- DiskLruCacheStore    // DiskLruCache
```

#### 7.1. 配置

```java
// 实例化时配置缓存管理
new SeHttp.Builder()
        .cacheStore(new DiskLruCacheStore(...))
        .build();
```

#### 7.2. 自定义缓存管理

你可以实现``CacheStore``接口，并在实例化时设置给``SeHttp``，自定义缓存管理：
```java
public interface CacheStore {
    <T> void put(String key, CacheEntity<T> cacheEntity);
    <T> CacheEntity<T> get(String key);
}
```

#### 7.3. 缓存请求

```java
seHttp.get(URL_GET)
        .cacheKey("key")    // 缓存Key
        .cachePolicy(CachePolicy.CACHE_THEN_REQUEST)    // 缓存策略
        .cacheDuration(1000 * 60 * 60 * 24)     // 缓存有效时长
        .execute(...)
```

#### 7.4. 缓存策略

``SeHttp``默认请求为```CachePolicy.NO_CACHE```模式。
```java
CachePolicy:
-- NO_CACHE,            // 不缓存
-- REQUEST_ELSE_CACHE,  // 优先请求网络，若失败，使用缓存
-- CACHE_ELSE_REQUEST,  // 优先使用缓存，若无，请求网络
-- CACHE_THEN_REQUEST   // 先使用缓存，无论是否成功，然后请求网络
```

#### 7.5. 缓存回调

相应策略下，若获取缓存成功，则回调```onCacheSuccess(T t)```

## 8. 取消请求

```java
// 取消对应tag请求
seHttp.cancelTag(tag);
// 取消所有请求
seHttp.cancelAll();
```

## 9. 混淆

```java
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
```
