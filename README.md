# SeHttp

#### 精简、高效的网络请求框架

[![](https://jitpack.io/v/senierr/SeHttp.svg)](https://jitpack.io/#senierr/SeHttp)
[![](https://img.shields.io/travis/rust-lang/rust.svg)](https://github.com/senierr/SeHttp)
[![](https://img.shields.io/badge/dependencies-okhttp-green.svg)](https://github.com/square/okhttp)
[![](https://img.shields.io/badge/dependencies-okio-green.svg)](https://github.com/square/okio)

> 此框架专注于高效、精简的网络请求，底层基于`okhttp3`，不参与任何数据持久化。

## 目前支持
* 普通get, post, put, delete, head, options, patch请求
* 自定义公共请求参数，请求头
* 自定义请求参数，请求头，请求体
* 多级别日志打印
* 301、302重定向
* 多种HTTPS验证
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

### 初始化

```java
SeHttp seHttp = SeHttp.Builder()
        .setDebug("SeHttp", LogLevel.BODY)               // 开启调试
        .setConnectTimeout(30 * 1000)       		 // 设置超时，默认30秒
        .setReadTimeout(30 * 1000)
        .setWriteTimeout(30 * 1000)
        .addInterceptor(...)                             // 添加应用层拦截器
        .addNetworkInterceptor(...)                      // 添加网络层拦截器
        .setHostnameVerifier(...)                        // 设置域名匹配规则
        .setCookieJar(...)                               // 设置自定义cookie管理
        .setSSLSocketFactory(...)                        // 设置SSL认证
        .addCommonHeader("key", "value")     		 // 添加全局头
        .addCommonUrlParam("key", "value")      	 // 添加全局参数
        .setRefreshInterval(200)                    // 设置异步刷新间隔
        .setRetryCount(3)                           // 设置失败重试次数
        .build();
```

### API

```java
seHttp.get(urlStr)				      // 请求方法：get、post、head、delete、put、options
        .addUrlParam("key", "value")                  // 添加URL参数
        .addHeader("header", "value")                 // 添加请求头
	.requestBody4Text(...)                	      // 设置文本请求
        .requestBody4JSon(...)      		      // 设置JSON请求
        .requestBody4Xml(...)                         // 设置XML请求
        .requestBody4Byte(...)                        // 设置字节流请求
        .addRequestParam("key", "param")              // 添加请求体参数，默认表单
	.addRequestParam("key", new File())           // 添加文件
	.setRequestBody(...)			      // 设置自定义请求体
	.execute()				      // 同步请求，返回Response
        .execute(...);		      // 异步请求，返回转换结果
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
