package com.raiser.rpc.config;

/**
 * @author: zhengyangxin
 * @date: 8/30/2022 9:54 PM
 */
public interface Constant {
    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/register";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

    String ZK_NAMESPACE = "raiser-rpc";

}
