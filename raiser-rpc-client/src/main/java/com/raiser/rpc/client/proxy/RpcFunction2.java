package com.raiser.rpc.client.proxy;

/**
 * @author: zhengyangxin
 * @date: 9/7/2022 2:21 PM
 */
@FunctionalInterface
public interface RpcFunction2<T, P1, P2> extends SerializableFunction<T> {
    Object apply(T t, P1 p1, P2 p2);
}
