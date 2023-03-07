package com.raiser.rpc.client.handler;

/**
 * @author: zhengyangxin
 * @date: 9/4/2022 5:19 PM
 */
public interface AsyncRPCCallback {
    void success(Object result);

    void fail(Exception e);
}
