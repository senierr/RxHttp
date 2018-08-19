package com.senierr.sehttp.cache;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cache;
import okhttp3.CipherSuite;
import okhttp3.Handshake;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.internal.Util;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.StatusLine;
import okhttp3.internal.io.FileSystem;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Source;

import static okhttp3.internal.Util.UTF_8;

/**
 * 缓存处理器
 *
 * @author zhouchunjie
 * @date 2018/8/18
 */
public class CacheEngine {

    private DiskLruCache diskLruCache;

    public CacheEngine(File directory, long maxSize) {
        diskLruCache = DiskLruCache.create(FileSystem.SYSTEM, directory, 1, 1, 1024 * 1024 * 10);
    }

//    /**
//     *
//     *
//     * @param key
//     * @return
//     */
//    private String key(String key) {
//        return ByteString.encodeUtf8(key).md5().hex();
//    }
//
//    private void write(String key, String value) {
//        try {
//            DiskLruCache.Editor editor = diskLruCache.edit(ByteString.encodeUtf8(key).md5().hex());
//            if (editor != null) {
//                BufferedSink sink = Okio.buffer(editor.newSink(0));
//                sink.write(value.getBytes());
//                sink.close();
//                editor.commit();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private Response read(String key) {
//        final DiskLruCache.Snapshot snapshot;
//        try {
//            snapshot = diskLruCache.get(ByteString.encodeUtf8(key).md5().hex());
//            if (snapshot != null) {
//                BufferedSource source = Okio.buffer(snapshot.getSource(0));
//                String value = source.readString(UTF_8);
//                printLog(value);
//                source.close();
//                snapshot.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        DiskLruCache.Snapshot snapshot;
//        Cache.Entry entry;
//        try {
//            snapshot = cache.get(key);
//            if (snapshot == null) {
//                return null;
//            }
//        } catch (IOException e) {
//            // Give up because the cache cannot be read.
//            return null;
//        }
//
//        try {
//            entry = new Cache.Entry(snapshot.getSource(ENTRY_METADATA));
//        } catch (IOException e) {
//            Util.closeQuietly(snapshot);
//            return null;
//        }
//
//        Response response = entry.response(snapshot);
//        if (!entry.matches(request, response)) {
//            Util.closeQuietly(response.body());
//            return null;
//        }
//
//        return response;
//    }
//
//    private static final class Entry {
//        /** Synthetic response header: the local time when the request was sent. */
//        private static final String SENT_MILLIS = Platform.get().getPrefix() + "-Sent-Millis";
//
//        /** Synthetic response header: the local time when the response was received. */
//        private static final String RECEIVED_MILLIS = Platform.get().getPrefix() + "-Received-Millis";
//
//        private final String url;
//        private final Headers varyHeaders;
//        private final String requestMethod;
//        private final Protocol protocol;
//        private final int code;
//        private final String message;
//        private final Headers responseHeaders;
//        private final @Nullable Handshake handshake;
//        private final long sentRequestMillis;
//        private final long receivedResponseMillis;
//
//        /**
//         * Reads an entry from an input stream. A typical entry looks like this:
//         * <pre>{@code
//         *   http://google.com/foo
//         *   GET
//         *   2
//         *   Accept-Language: fr-CA
//         *   Accept-Charset: UTF-8
//         *   HTTP/1.1 200 OK
//         *   3
//         *   Content-Type: image/png
//         *   Content-Length: 100
//         *   Cache-Control: max-age=600
//         * }</pre>
//         *
//         * <p>A typical HTTPS file looks like this:
//         * <pre>{@code
//         *   https://google.com/foo
//         *   GET
//         *   2
//         *   Accept-Language: fr-CA
//         *   Accept-Charset: UTF-8
//         *   HTTP/1.1 200 OK
//         *   3
//         *   Content-Type: image/png
//         *   Content-Length: 100
//         *   Cache-Control: max-age=600
//         *
//         *   AES_256_WITH_MD5
//         *   2
//         *   base64-encoded peerCertificate[0]
//         *   base64-encoded peerCertificate[1]
//         *   -1
//         *   TLSv1.2
//         * }</pre>
//         * The file is newline separated. The first two lines are the URL and the request method. Next
//         * is the number of HTTP Vary request header lines, followed by those lines.
//         *
//         * <p>Next is the response status line, followed by the number of HTTP response header lines,
//         * followed by those lines.
//         *
//         * <p>HTTPS responses also contain SSL session information. This begins with a blank line, and
//         * then a line containing the cipher suite. Next is the length of the peer certificate chain.
//         * These certificates are base64-encoded and appear each on their own line. The next line
//         * contains the length of the local certificate chain. These certificates are also
//         * base64-encoded and appear each on their own line. A length of -1 is used to encode a null
//         * array. The last line is optional. If present, it contains the TLS version.
//         */
//        Entry(Source in) throws IOException {
//            try {
//                BufferedSource source = Okio.buffer(in);
//                url = source.readUtf8LineStrict();
//                requestMethod = source.readUtf8LineStrict();
//                Headers.Builder varyHeadersBuilder = new Headers.Builder();
//                int varyRequestHeaderLineCount = readInt(source);
//                for (int i = 0; i < varyRequestHeaderLineCount; i++) {
//                    varyHeadersBuilder.addLenient(source.readUtf8LineStrict());
//                }
//                varyHeaders = varyHeadersBuilder.build();
//
//                StatusLine statusLine = StatusLine.parse(source.readUtf8LineStrict());
//                protocol = statusLine.protocol;
//                code = statusLine.code;
//                message = statusLine.message;
//                Headers.Builder responseHeadersBuilder = new Headers.Builder();
//                int responseHeaderLineCount = readInt(source);
//                for (int i = 0; i < responseHeaderLineCount; i++) {
//                    responseHeadersBuilder.addLenient(source.readUtf8LineStrict());
//                }
//                String sendRequestMillisString = responseHeadersBuilder.get(SENT_MILLIS);
//                String receivedResponseMillisString = responseHeadersBuilder.get(RECEIVED_MILLIS);
//                responseHeadersBuilder.removeAll(SENT_MILLIS);
//                responseHeadersBuilder.removeAll(RECEIVED_MILLIS);
//                sentRequestMillis = sendRequestMillisString != null
//                        ? Long.parseLong(sendRequestMillisString)
//                        : 0L;
//                receivedResponseMillis = receivedResponseMillisString != null
//                        ? Long.parseLong(receivedResponseMillisString)
//                        : 0L;
//                responseHeaders = responseHeadersBuilder.build();
//
//                if (isHttps()) {
//                    String blank = source.readUtf8LineStrict();
//                    if (blank.length() > 0) {
//                        throw new IOException("expected \"\" but was \"" + blank + "\"");
//                    }
//                    String cipherSuiteString = source.readUtf8LineStrict();
//                    CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
//                    List<Certificate> peerCertificates = readCertificateList(source);
//                    List<Certificate> localCertificates = readCertificateList(source);
//                    TlsVersion tlsVersion = !source.exhausted()
//                            ? TlsVersion.forJavaName(source.readUtf8LineStrict())
//                            : TlsVersion.SSL_3_0;
//                    handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates);
//                } else {
//                    handshake = null;
//                }
//            } finally {
//                in.close();
//            }
//        }
//
//        Entry(Response response) {
//            this.url = response.request().url().toString();
//            this.varyHeaders = HttpHeaders.varyHeaders(response);
//            this.requestMethod = response.request().method();
//            this.protocol = response.protocol();
//            this.code = response.code();
//            this.message = response.message();
//            this.responseHeaders = response.headers();
//            this.handshake = response.handshake();
//            this.sentRequestMillis = response.sentRequestAtMillis();
//            this.receivedResponseMillis = response.receivedResponseAtMillis();
//        }
//
//        public void writeTo(DiskLruCache.Editor editor) throws IOException {
//            BufferedSink sink = Okio.buffer(editor.newSink(ENTRY_METADATA));
//
//            sink.writeUtf8(url)
//                    .writeByte('\n');
//            sink.writeUtf8(requestMethod)
//                    .writeByte('\n');
//            sink.writeDecimalLong(varyHeaders.size())
//                    .writeByte('\n');
//            for (int i = 0, size = varyHeaders.size(); i < size; i++) {
//                sink.writeUtf8(varyHeaders.name(i))
//                        .writeUtf8(": ")
//                        .writeUtf8(varyHeaders.value(i))
//                        .writeByte('\n');
//            }
//
//            sink.writeUtf8(new StatusLine(protocol, code, message).toString())
//                    .writeByte('\n');
//            sink.writeDecimalLong(responseHeaders.size() + 2)
//                    .writeByte('\n');
//            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                sink.writeUtf8(responseHeaders.name(i))
//                        .writeUtf8(": ")
//                        .writeUtf8(responseHeaders.value(i))
//                        .writeByte('\n');
//            }
//            sink.writeUtf8(SENT_MILLIS)
//                    .writeUtf8(": ")
//                    .writeDecimalLong(sentRequestMillis)
//                    .writeByte('\n');
//            sink.writeUtf8(RECEIVED_MILLIS)
//                    .writeUtf8(": ")
//                    .writeDecimalLong(receivedResponseMillis)
//                    .writeByte('\n');
//
//            if (isHttps()) {
//                sink.writeByte('\n');
//                sink.writeUtf8(handshake.cipherSuite().javaName())
//                        .writeByte('\n');
//                writeCertList(sink, handshake.peerCertificates());
//                writeCertList(sink, handshake.localCertificates());
//                sink.writeUtf8(handshake.tlsVersion().javaName()).writeByte('\n');
//            }
//            sink.close();
//        }
//
//        private boolean isHttps() {
//            return url.startsWith("https://");
//        }
//
//        private List<Certificate> readCertificateList(BufferedSource source) throws IOException {
//            int length = readInt(source);
//            if (length == -1) return Collections.emptyList(); // OkHttp v1.2 used -1 to indicate null.
//
//            try {
//                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//                List<Certificate> result = new ArrayList<>(length);
//                for (int i = 0; i < length; i++) {
//                    String line = source.readUtf8LineStrict();
//                    Buffer bytes = new Buffer();
//                    bytes.write(ByteString.decodeBase64(line));
//                    result.add(certificateFactory.generateCertificate(bytes.inputStream()));
//                }
//                return result;
//            } catch (CertificateException e) {
//                throw new IOException(e.getMessage());
//            }
//        }
//
//        private void writeCertList(BufferedSink sink, List<Certificate> certificates)
//                throws IOException {
//            try {
//                sink.writeDecimalLong(certificates.size())
//                        .writeByte('\n');
//                for (int i = 0, size = certificates.size(); i < size; i++) {
//                    byte[] bytes = certificates.get(i).getEncoded();
//                    String line = ByteString.of(bytes).base64();
//                    sink.writeUtf8(line)
//                            .writeByte('\n');
//                }
//            } catch (CertificateEncodingException e) {
//                throw new IOException(e.getMessage());
//            }
//        }
//
//        public boolean matches(Request request, Response response) {
//            return url.equals(request.url().toString())
//                    && requestMethod.equals(request.method())
//                    && HttpHeaders.varyMatches(response, varyHeaders, request);
//        }
//
//        public Response response(DiskLruCache.Snapshot snapshot) {
//            String contentType = responseHeaders.get("Content-Type");
//            String contentLength = responseHeaders.get("Content-Length");
//            Request cacheRequest = new Request.Builder()
//                    .url(url)
//                    .method(requestMethod, null)
//                    .headers(varyHeaders)
//                    .build();
//            return new Response.Builder()
//                    .request(cacheRequest)
//                    .protocol(protocol)
//                    .code(code)
//                    .message(message)
//                    .headers(responseHeaders)
//                    .body(new Cache.CacheResponseBody(snapshot, contentType, contentLength))
//                    .handshake(handshake)
//                    .sentRequestAtMillis(sentRequestMillis)
//                    .receivedResponseAtMillis(receivedResponseMillis)
//                    .build();
//        }
//    }
}
