package com.raiser.rpc.client.route.impl;

import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.client.route.RpcLoadBalance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhengyangxin
 * @date: 9/1/2022 10:40 PM
 */
public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {
    private static AtomicInteger roundRobin = new AtomicInteger(0);

    private RpcProtocol doRoute(List<RpcProtocol> protocols) {
        int size = protocols.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return protocols.get(index);
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
