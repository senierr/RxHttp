# RxHttp

[![](https://img.shields.io/badge/release-v1.2.0-blue.svg)](https://github.com/senierr/RxHttp)
[![](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/senierr/RxHttp)
[![](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

### 目前支持

**普通get, post, put, delete, head, options, patch请求**  
**自定义请求**  
**自定义基础请求参数、请求头**  
**自定义请求参数、请求头、请求体**  
**任意请求进度监听**  
**多种HTTPS验证**  
**可扩展Cookie管理**  
**多级别日志打印**  
**可扩展数据解析**  
**简洁的链式调用**  
**同步支持RxJava2**  

### 1.仓库导入

```
implementation 'com.senierr.http:rxhttp:最新版本'
```

**`RxHttp`内部关联依赖：**

```
-- 'com.squareup.okhttp3:okhttp:3.11.0'
-- 'io.reactivex.rxjava2:rxjava:2.1.10'
```

### 2.权限

```
<uses-permission android:name="android.permission.INTERNET"/>

// 文件上传下载需要以下权限
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### 3.实例化

```
val rxHttp = RxHttp.Builder()
        .debug(...)                 // 开启Debug模式
        .baseUrl(...)               // 设置基础请求地址
        .addBaseHeader(...)         // 增加单个基础头
        .addBaseHeaders(...)        // 增加多个基础头
        .addBaseUrlParam(...)       // 增加单个基础URL参数
        .addBaseUrlParams(...)      // 增加多个基础URL参数
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

### 4.构建请求

```
// 通过RxHttp实例发起请求
rxHttp.get(...)  // 支持get、post、head、delete、put、options、trace、method(自定义请求)
        .setBaseUrl(...)                // 设置基础请求地址（只作用于当前请求，不会改变公共设置）
        .ignoreBaseUrl()                // 忽略基础请求地址（只作用于当前请求，不会改变公共设置）
        .ignoreBaseUrlParams()          // 忽略基础请求参数（只作用于当前请求，不会改变公共设置）
        .ignoreBaseHeaders()            // 忽略基础请求头（只作用于当前请求，不会改变公共设置）
        .addHeader(...)                 // 增加单个头
        .addHeaders(...)                // 增加多个头
        .addUrlParam(...)               // 增加单个URL参数
        .addUrlParams(...)              // 增加多个URL参数
        .addRequestParam(...)           // 增加单个表单参数
        .addRequestStringParams(...)    // 增加多个字符串表单参数
        .addRequestFileParams(...)      // 增加多个文件表单参数
        .requestBody4JSon(...)       // 设置Json请求体
        .requestBody4Text(...)       // 设置Text请求体
        .requestBody4Xml(...)        // 设置XML请求体
        .requestBody4Byte(...)       // 设置Byte请求体
        .requestBody4File(...)       // 设置File请求体
        .requestBody(...)            // 自定义请求体
        .isMultipart(...)               // 是否分片表单
        .toUploadObservable(...)        // 转为带上传进度的Observable
        .toDownloadObservable(...)      // 转为带下载进度的Observable
        .toResultObservable(...)        // 转为普通结果的Observable
```

### 5.数据解析

在发起请求时，必须设置**Converter**，以便``RxHttp``能正确解析数据。

``RxHttp``内置了三种``Converter``：
* **DefaultConverter**: 返回Response
* **StringConverter**: 返回String
* **FileConverter**: 下载文件并返回File

当然，您也可以实现``Converter<T>``接口，自定义数据解析规则：
```
public interface Converter<T> {
    @NonNull T convertResponse(@NonNull Response response) throws Throwable;
}
```

### 6.结果返回

返回结果的类型为``Observable<T>``，其中泛型``<T>``即为解析的结果类型。

### 7.进度监听

``RxHttp``将``上传进度监听``和``下载进度监听``进行了剥离，并使其适用于**任意请求**。

在发起请求时，您可以构建**带上传进度的Observable**以及**带下载进度的Observable**，并可通过内置的操作符监听进度。

``RxHttp``提供了操作符**ProgressProcessor**，来进行进度处理。

示例如下:
```
rxHttp.get("...")
    .toDownloadObservable(FileConverter(...))
    .compose(object : ProgressProcessor<File>() {
        override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
            // 进度处理
            ...
        }
    })
    .subscribe()
```

### 8.Cookie

``RxHttp``提供以下方式持久化管理``Cookie``：
```
-- SPCookieJar      // SharedPreferences
```

#### 8.1.配置

```
// 1. 实例化
SPCookieStore cookieStore = new SPCookieStore(context);
// 2. 配置
new RxHttp.Builder()
        .cookieJar(cookieStore.getCookieJar())
        .build();
```

#### 8.2.接口说明
```
// 获取Okhttp3的CookieJar
CookieJar getCookieJar();
// 是否过期
boolean isExpired(Cookie cookie);
// 保存多个Cookie
void saveCookies(HttpUrl url, List<Cookie> cookies);
// 保存单个Cookie
void saveCookie(HttpUrl url, Cookie cookie);
// 获取URL对应所有Cookie
List<Cookie> getCookies(HttpUrl url);
// 获取所有Cookie
List<Cookie> getAllCookie();
// 移除单个URL对应Cookie
void removeCookie(HttpUrl url, Cookie cookie);
// 移除URL对应所有Cookie
void removeCookies(HttpUrl url);
// 移除所有Cookie
void clear();
```

#### 8.3.自定义

通过实现``CookieStore``接口，自定义Cookie管理方式。

### 9.HTTPS

```
// 1. 实例化
SSLFactory sslFactory = new SSLFactory(...);
// 2. 配置
new RxHttp.Builder()
        .sslSocketFactory(sslFactory.getsSLSocketFactory(),
                          sslFactory.getTrustManager())
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

### 10.混淆

```
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
```

### License

```
Copyright 2018 senierr

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
