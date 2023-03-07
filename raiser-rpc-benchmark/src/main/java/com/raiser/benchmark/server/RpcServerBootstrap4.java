package com.raiser.benchmark.server;

import com.raiser.benchmark.service.UserService;
import com.raiser.benchmark.service.impl.UserServiceServerImpl;
import com.raiser.rpc.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 3:50 PM
 */
public class RpcServerBootstrap4 {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerBootstrap4.class);

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:18888";
        String registryAddress = "127.0.0.1:2181";
        RpcServer rpcServer = new RpcServer(serverAddress, registryAddress);
        UserService userService1 = new UserServiceServerImpl();
        rpcServer.addService(UserService.class.getName(), "1.0", userService1);
        try {
            rpcServer.start();
        } catch (Exception ex) {
            logger.error("Exception: {}", ex.toString());
        }
    }
}
