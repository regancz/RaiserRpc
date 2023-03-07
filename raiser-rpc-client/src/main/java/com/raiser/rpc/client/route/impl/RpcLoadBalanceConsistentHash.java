package com.raiser.rpc.client.route.impl;

import com.google.common.hash.Hashing;
import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.client.route.RpcLoadBalance;

import java.util.List;
import java.util.Map;

/**
 * @author: zhengyangxin
 * @date: 9/3/2022 2:50 PM
 */
public class RpcLoadBalanceConsistentHash extends RpcLoadBalance {
    public RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
        return addressList.get(index);
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
