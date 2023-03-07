package com.raiser.rpc.client.proxy;

/**
 * @author: zhengyangxin
 * @date: 9/7/2022 2:21 PM
 */
@FunctionalInterface
public interface RpcFunction<T, P> extends SerializableFunction<T> {
    Object apply(T t, P p) throws InterruptedException;
}
