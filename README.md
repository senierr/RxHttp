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
* 自定义失败重连次数
* 根据Tag取消请求
* 链式调用
* 可扩展回调

## 前言
#### 为什么取消单例模式？
假设这么一种场景：有两个业务模块，需要设置不同的的SSL加密方式和公共参数。
旧版（1.X.X）由于是单例模式，只可以设置一种加密模式和公共参数，无法为各请求模块独立设置公共参数。
新版（2.X.X）的SeHttp将单例模式设置上浮至使用者，使其更灵活适应不同的业务模块。例如，你可以给注册和登录模块设置各自的请求器(RegisterHttp: SeHttp和LoginHttp: SeHttp)并配置不同的参数。


## 基本用法

#### 1. 导入仓库：

```java
maven { url 'https://jitpack.io' }
```

#### 2. 添加依赖

```java
implementation 'com.github.senierr:SeHttp:<release_version>'
```

`SeHttp`底层基于`okhttp3`，所以默认依赖：

```java
implementation 'com.squareup.okhttp3:okhttp:3.9.1'
```

#### 3. 添加权限

```java
<uses-permission android:name="android.permission.INTERNET"/>
// 文件下载需要以下权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 实例化

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
-- refreshInterval         // 设置上传下载进度回调最小间隔时间(ms)
-- debug                   // 设置Debug模式
-- connectTimeout          // 设置连接超时(ms)
-- readTimeout             // 设置读超时(ms)
-- writeTimeout            // 设置写超时(ms)
-- hostnameVerifier        // 设置域名校验规则
-- sslFactory              // 设置SSL验证
-- cookieJar               // 设置Cookie管理
-- addInterceptor          // 增加拦截器
-- addNetworkInterceptor   // 增加网络层拦截器
```

### 请求

```java
// 创建请求器
RequestFactory requestFactory = new RequestFactory(this, method, urlStr);
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
-- build                    // 构造Request
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

## 请求回调

``SeHttp``基础回调``BaseCallback``如下：
```java
// Reponse解析：异步线程
public abstract T convert(Response response) throws Exception;

// 上传监听：主线程
public void onUpload(int progress, long currentSize, long totalSize) {}

// 下载监听：主线程
public void onDownload(int progress, long currentSize, long totalSize) {}

// 成功回调：主线程
public abstract void onSuccess(T t);

// 失败回调：主线程
public void onFailure(Exception e) {}
```
同时还提供了扩展的``StringCallback``、``JsonCallback``、``FileCallback``。

### 自定义回调

> ``BaseCallback``中，``convert()``的作用是将``Reponse``解析成需要返回的数据。
> ``SeHttp``内部提供的``StringCallback``、``JsonCallback``、``FileCallback``就是继承``BaseCallback``通过重写``convert()``，实现不同的功能。

了解其原理，那么现在，你也就能通过重写``convert()``自定义适合自己项目的``Callback``了

### 特殊回调

``JsonCallback``扩展了其特有的回调：
```java
// 解析Json数据：异步线程
public abstract T parseJson(String responseStr) throws Exception;
```

### 字符集

对于``StringCallback``、``JsonCallback``，若不设置特殊字符集，默认返回UTF-8类型的字符串。若要设置返回字符集，可通过构造器传入：
```java
public StringCallback(Charset charset)
public JsonCallback(Charset charset)
```

## Cookie

```java
// 实例化时设置Cookie自动管理
new SeHttp.Builder()
        .cookieJar(new SPCookieJar(this))   // SharedPreferences存储
        .build();

// 主动管理Cookie
ClearableCookieJar cookieJar = seHttp.getCookieJar();   // 获取管理器

cookieJar.saveCookie(httpUrl, cookie);      // 保存单个URL对应Cookie
cookieJar.saveCookies(httpUrl, cookies);    // 保存多个URL对应Cookie
cookieJar.getAllCookie();                   // 获取所有Cookie
cookieJar.getCookies(httpUrl);              // 获取URL对应所有Cookie
cookieJar.removeCookie(httpUrl, cookie);    // 移除单个URL对应Cookie
cookieJar.removeCookies(httpUrl);           // 移除URL对应所有Cookie
cookieJar.removeAllCookie();                // 移除所有Cookie
```

当然，你也可以通过继承``ClearableCookieJar``实现自己的管理方式。

> 后期会推出多种管理方式

## HTTPS

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

## 取消请求

```java
// 取消对应tag请求
seHttp.cancelTag(tag);
// 取消所有请求
seHttp.cancelAll();
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
