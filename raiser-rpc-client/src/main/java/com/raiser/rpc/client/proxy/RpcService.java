package com.raiser.rpc.client.proxy;

import com.raiser.rpc.client.handler.RpcFuture;

/**
 * @author: zhengyangxin
 * @date: 9/4/2022 2:09 PM
 */
public interface RpcService<T, P, FN extends SerializableFunction<T>> {
    RpcFuture call(String funcName, Object... args) throws Exception;

    /**
     * lambda method reference
     */
    RpcFuture call(FN fn, Object... args) throws Exception;
}
