package com.raiser.rpc.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 8:28 PM
 */
public class ThreadPoolUtil {
    public static ThreadPoolExecutor makeServerThreadPool(String serviceName) {
        return new ThreadPoolExecutor(
                2 * Runtime.getRuntime().availableProcessors(),
                4 * Runtime.getRuntime().availableProcessors(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> new Thread(r, "netty-rpc-" + serviceName + "-" + r.hashCode()),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public static ThreadPoolExecutor makeClientThreadPool() {
        return new ThreadPoolExecutor(
                2 * Runtime.getRuntime().availableProcessors(),
                2 * Runtime.getRuntime().availableProcessors(),
                600L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000)
        );
    }
}
