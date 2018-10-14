# RxHttp

[![](https://jitpack.io/v/senierr/RxHttp.svg)](https://jitpack.io/#senierr/RxHttp)
[![](https://img.shields.io/travis/rust-lang/rust.svg)](https://github.com/senierr/RxHttp)
[![](https://img.shields.io/badge/dependencies-okhttp-green.svg)](https://github.com/square/okhttp)
[![](https://img.shields.io/badge/dependencies-okio-green.svg)](https://github.com/square/okio)

#### 精简、高效且有趣的网络请求框架

> 此库主要用于探索**retrofit**、**okhttp**等一系列网络请求库，在使用方面的不足和遗憾。
> **注意：版本之间API差异可能较大，请谨慎升级！**

## 目前支持
* 普通get, post, put, delete, head, options, patch请求
* 自定义请求
* 自定义公共请求参数、请求头
* 自定义请求参数、请求头、请求体
* 请求进度监听，包括但不仅限于：上传、下载
* 多种HTTPS验证
* 可扩展Cookie管理
* 多级别日志打印
* 请求重定向
* 可扩展数据解析
* 链式调用
* 支持RxJava2

## 工作流程

> 主要工作流程可以概括为：**构造** -> **请求** -> **解析** -> **返回**

## 1. 基本使用

#### 1.1. 导入仓库：

```java
maven { url 'https://jitpack.io' }
```

#### 1.2. 添加依赖

```java
implementation 'com.github.senierr:RxHttp:<release_version>'
```

**注：`RxHttp`内部关联依赖：**

```java
-- 'com.android.support:support-annotations:27.1.1'
-- 'com.squareup.okhttp3:okhttp:3.11.0'
-- 'io.reactivex.rxjava2:rxjava:2.1.10'
```

#### 1.3. 添加权限

```java
<uses-permission android:name="android.permission.INTERNET"/>
// 文件上传下载需要以下权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

#### 1.4. 实例化

```java
val rxHttp = RxHttp.Builder()
        .debug(...)                 // 开启Debug模式
        .addCommonHeader(...)       // 增加单个公共头
        .addCommonHeaders(...)      // 增加多个公共头
        .addCommonUrlParam(...)     // 增加单个公共URL参数
        .addCommonUrlParams(...)    // 增加多个公共URL参数
        .connectTimeout(...)        // 设置连接超时(ms)
        .readTimeout(...)           // 设置读超时(ms)
        .writeTimeout(...)          // 设置写超时(ms)
        .hostnameVerifier(...)      // 设置域名校验规则
        .sslFactory(...)            // 设置SSL验证
        .cookieJar(...)             // 设置Cookie管理
        .addInterceptor(...)        // 增加拦截器
        .addNetworkInterceptor(...) // 增加网络层拦截器
        .build()
```

#### 1.5. 发起请求

```java
// 通过RxHttp实例发起请求
rxHttp.get(...)  // 支持get、post、head、delete、put、options、trace、method(自定义请求)
        .addHeader(...)                 // 增加单个头
        .addHeaders(...)                // 增加多个头
        .addUrlParam(...)               // 增加单个URL参数
        .addUrlParams(...)              // 增加多个URL参数
        .addRequestParam(...)           // 增加单个表单参数
        .addRequestStringParams(...)    // 增加多个字符串表单参数
        .addRequestFileParams(...)      // 增加多个文件表单参数
        .setRequestBody4JSon(...)       // 设置Json请求体
        .setRequestBody4Text(...)       // 设置Text请求体
        .setRequestBody4Xml(...)        // 设置XML请求体
        .setRequestBody4Byte(...)       // 设置Byte请求体
        .setRequestBody4File(...)       // 设置File请求体
        .setRequestBody(...)            // 自定义请求体
        .isMultipart(...)               // 是否分片表单
        .setOnUploadListener(...)       // 设置上传进度监听
        .setOnDownloadListener(...)     // 设置下载进度监听
        .generateRequest()              // 创建Okhttp请求
        .execute(...)                   // 发起请求
```

## 2. 数据解析

``RxHttp``在发起请求``execute(...)``时需要传入数据解析器：``Converter<T>``，以便返回所需的正确结果。

``RxHttp``内置了两种``Converter``: ``StringConverter(字符串结果)``和``FileConverter(文件存储)``

当然，你也可以自定义``Converter<T>``，并返回自己需要的数据类型：
```java
public interface Converter<T> {
    @NonNull T convertResponse(@NonNull Response response) throws Throwable;
}
```

## 3. 请求结果

返回结果的类型为``Observable<Response<T>>``，其中泛型``<T>``就是解析的结果类型。

## 4. 进度监听

``RxHttp``将``进度监听``和``返回结果``进行了剥离，并使其适用于**任意请求**。

```java
public interface OnProgressListener {
    void onProgress(@NonNull Progress progress);
}

// 在发起请求时，可以分别设置：
-- setOnUploadListener(...)       // 设置上传数据进度监听
-- setOnDownloadListener(...)     // 设置下载数据进度监听
```

## 5. Cookie

``RxHttp``提供以下方式持久化管理``Cookie``：
```java
-- SPCookieJar      // SharedPreferences
```
#### 5.1. 配置

```java
// 实例化时配置Cookie管理
new RxHttp.Builder()
        .cookieJar(new SPCookieJar(this))
        .build();
```

#### 5.2. 管理

```java
ClearableCookieJar cookieJar = rxHttp.getCookieJar();   // 获取管理器

cookieJar.saveCookie(httpUrl, cookie);      // 保存单个URL对应Cookie
cookieJar.saveCookies(httpUrl, cookies);    // 保存多个URL对应Cookie
cookieJar.getAllCookie();                   // 获取所有Cookie
cookieJar.getCookies(httpUrl);              // 获取URL对应所有Cookie
cookieJar.removeCookie(httpUrl, cookie);    // 移除单个URL对应Cookie
cookieJar.removeCookies(httpUrl);           // 移除URL对应所有Cookie
cookieJar.clear();                          // 移除所有Cookie
```

#### 5.3. 自定义管理

通过继承``ClearableCookieJar``，并实现其抽象方法，在实例化时设置给``RxHttp``，自定义Cookie管理方式。

```java
public abstract void saveCookies(HttpUrl url, List<Cookie> cookies);

public abstract void saveCookie(HttpUrl url, Cookie cookie);

public abstract List<Cookie> getCookies(HttpUrl url);

public abstract List<Cookie> getAllCookie();

public abstract boolean removeCookie(HttpUrl url, Cookie cookie);

public abstract boolean removeCookies(HttpUrl url);

public abstract boolean clear();
```

## 6. HTTPS

```java
// 实例化时设置SSL验证
new RxHttp.Builder()
        .sslFactory(new SSLFactory(...))
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

## 7. 混淆

```java
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
```
