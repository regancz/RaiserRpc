package com.raiser.rpc.client.handler;

import com.raiser.rpc.client.RpcClient;
import com.raiser.rpc.codec.RpcRequest;
import com.raiser.rpc.codec.RpcResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: zhengyangxin
 * @date: 9/2/2022 2:23 PM
 */
public class RpcFuture implements Future {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);

    // achieve get result async
    private final Sync sync;
    private final RpcRequest request;
    private RpcResponse response;
    private final long startTime;
    private long responseTimeThreshold = 5000;
    private final List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
    // guarantee pending callbacks concurrent, achieve get response async
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                return compareAndSetState(pending, done);
            } else {
                return true;
            }
        }

        protected boolean isDone() {
            return getState() == done;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() {
        sync.acquire(1);
        if (this.response != null) {
            return this.response.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    public void done(RpcResponse response) {
        this.response = response;
        sync.release(1);
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {

            logger.warn("Service time out. Request id = " + response.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    public void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } catch (Exception e) {
            lock.unlock();
        }
    }

    private void runCallback(AsyncRPCCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(() -> {
            if (!res.isError()) {
                callback.success(res.getResult());
            } else {
                callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
            }
        });
    }

    public RpcFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            // when adding callback, if you get result, we can unlock and run callback
            if (isDone()) {
                runCallback(callback);
            } else {
                // if you can't get result, adding callback
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }
}
