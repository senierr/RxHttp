package com.senierr.sehttp.internal;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.internal.Util;

/**
 * 线程调度器
 *
 * @author zhouchunjie
 * @date 2018/8/19
 */
public final class Dispatcher {

    /** 主线程异步回调最小间隔时长(ms) */
    private int refreshInterval = 100;

    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private Runnable idleCallback;

    private ExecutorService executorService;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Deque<CacheRealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    private final Deque<CacheRealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    private final Deque<CacheRealCall> runningSyncCalls = new ArrayDeque<>();

    public Dispatcher() {}

    public Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return executorService;
    }

    public synchronized int getRefreshInterval() {
        return refreshInterval;
    }

    public synchronized void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public synchronized void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
        promoteCalls();
    }

    public synchronized int getMaxRequests() {
        return maxRequests;
    }

    public synchronized void setMaxRequestsPerHost(int maxRequestsPerHost) {
        if (maxRequestsPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
        }
        this.maxRequestsPerHost = maxRequestsPerHost;
        promoteCalls();
    }

    public synchronized int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    /** 异步执行Call */
    public synchronized void enqueue(CacheRealCall.AsyncCall call) {
        if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }
    }

    /** 主线程执行 */
    public synchronized void enqueueOnMainThread(Runnable runnable) {
        if (runnable != null) {
            mainHandler.post(runnable);
        }
    }

    public synchronized void cancelTag(Object tag) {
        for (CacheRealCall.AsyncCall call : readyAsyncCalls) {
            if (tag.equals(call.request().tag())) {
                call.get().cancel();
            }
        }

        for (CacheRealCall.AsyncCall call : runningAsyncCalls) {
            if (tag.equals(call.request().tag())) {
                call.get().cancel();
            }
        }

        for (CacheRealCall call : runningSyncCalls) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public synchronized void cancelAll() {
        for (CacheRealCall.AsyncCall call : readyAsyncCalls) {
            call.get().cancel();
        }

        for (CacheRealCall.AsyncCall call : runningAsyncCalls) {
            call.get().cancel();
        }

        for (CacheRealCall call : runningSyncCalls) {
            call.cancel();
        }
    }

    private void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<CacheRealCall.AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            CacheRealCall.AsyncCall call = i.next();

            if (runningCallsForHost(call) < maxRequestsPerHost) {
                i.remove();
                runningAsyncCalls.add(call);
                executorService().execute(call);
            }

            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }

    private int runningCallsForHost(CacheRealCall.AsyncCall call) {
        int result = 0;
        for (CacheRealCall.AsyncCall c : runningAsyncCalls) {
            if (c.host().equals(call.host())) result++;
        }
        return result;
    }

    public synchronized void executed(CacheRealCall call) {
        runningSyncCalls.add(call);
    }

    public void finished(CacheRealCall.AsyncCall call) {
        finished(runningAsyncCalls, call, true);
    }

    public void finished(CacheRealCall call) {
        finished(runningSyncCalls, call, false);
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            if (promoteCalls) promoteCalls();
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized List<Call> queuedCalls() {
        List<Call> result = new ArrayList<>();
        for (CacheRealCall.AsyncCall asyncCall : readyAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<Call> runningCalls() {
        List<Call> result = new ArrayList<>();
        result.addAll(runningSyncCalls);
        for (CacheRealCall.AsyncCall asyncCall : runningAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int queuedCallsCount() {
        return readyAsyncCalls.size();
    }

    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }
}
