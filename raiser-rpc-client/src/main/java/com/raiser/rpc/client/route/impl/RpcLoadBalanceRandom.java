package com.raiser.rpc.client.route.impl;

import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.client.route.RpcLoadBalance;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author: zhengyangxin
 * @date: 9/3/2022 3:05 PM
 */
public class RpcLoadBalanceRandom extends RpcLoadBalance {
    private Random random = new Random();

    public RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        return addressList.get(random.nextInt(size));
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
