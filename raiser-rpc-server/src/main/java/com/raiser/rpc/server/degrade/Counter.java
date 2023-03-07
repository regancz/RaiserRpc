package com.raiser.rpc.server.degrade;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author charles
 * @date 2022/10/31 17:46
 */
public class Counter {

    // Closed 状态进入 Open 状态的错误个数阀值
    private final int failureCount;

    // failureCount 统计时间窗口
    private final long failureTimeInterval;

    // 当前错误次数
    private final AtomicInteger currentCount;

    // 上一次调用失败的时间戳
    private long lastTime;

    // Half-Open 状态下成功次数
    private final AtomicInteger halfOpenSuccessCount;

    public Counter(int failureCount, long failureTimeInterval) {
        this.failureCount = failureCount;
        this.failureTimeInterval = failureTimeInterval;
        this.currentCount = new AtomicInteger(0);
        this.halfOpenSuccessCount = new AtomicInteger(0);
        this.lastTime = System.currentTimeMillis();
    }

    /**
     * 失败次数 + 1
     * synchronized 保证线程安全
     * @return
     */
    public synchronized int incrFailureCount() {
        long current = System.currentTimeMillis();
        // 超过时间窗口，当前失败次数重置为 0
        if (current - lastTime > failureTimeInterval) {
            lastTime = current;
            currentCount.set(0);
        }
        return currentCount.getAndIncrement();
    }

    /**
     * half Open 状态下 成功数 + 1
     * @return
     */
    public int incrSuccessHalfOpenCount() {
        return this.halfOpenSuccessCount.incrementAndGet();
    }

    /**
     * 失败总次数是否超过阈值
     * @return
     */
    public boolean failureThresholdReached() {
        return getCurCount() >= failureCount;
    }

    public int getCurCount() {
        return currentCount.get();
    }

    /**
     * 重置
     */
    public synchronized void reset() {
        halfOpenSuccessCount.set(0);
        currentCount.set(0);
    }

}

