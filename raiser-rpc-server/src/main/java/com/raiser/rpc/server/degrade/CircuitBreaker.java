package com.raiser.rpc.server.degrade;

import java.util.function.Function;

import static com.raiser.rpc.server.degrade.State.*;

/**
 * @author charles
 * @date 2022/10/31 17:46
 */
public class CircuitBreaker {
    public State state;

    public final Config config;

    public final Counter counter;

    public long lastOpenedTime;

    public Function<Throwable, String> fallback;

    public CircuitBreaker(Config config) {
        this.counter = new Counter(config.getFailureCount(), config.getFailureTimeInterval());
        this.state = CLOSED;
        this.config = config;
    }

    /**
     * 判断是否 Open是否超时， 如果是则进入 halfOpen状态
     * @return
     */
    public boolean halfOpenTimeout() {
        return System.currentTimeMillis() - lastOpenedTime > config.getHalfOpenTimeout();
    }

    public void closed() {
        counter.reset();
        state = CLOSED;
    }

    public void open() {
        state = OPEN;
        lastOpenedTime = System.currentTimeMillis();
    }

    public void halfOpen() {
        state = HALF_OPEN;
    }

}

