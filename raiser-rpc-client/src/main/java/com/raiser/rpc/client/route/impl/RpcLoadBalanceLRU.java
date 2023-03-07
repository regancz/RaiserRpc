package com.raiser.rpc.client.route.impl;

import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.client.route.RpcLoadBalance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: zhengyangxin
 * @date: 9/3/2022 4:32 PM
 */
public class RpcLoadBalanceLRU extends RpcLoadBalance {
    private ConcurrentMap<String, LinkedHashMap<RpcProtocol, RpcProtocol>> jobMap =
            new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    private RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        LinkedHashMap<RpcProtocol, RpcProtocol> lruHashMap = jobMap.get(serviceKey);
        if (lruHashMap == null) {
            lruHashMap = new LinkedHashMap<RpcProtocol, RpcProtocol>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<RpcProtocol, RpcProtocol> eldest) {
                    return super.size() > 1000;
                }
            };
            jobMap.putIfAbsent(serviceKey, lruHashMap);
        }

        for (RpcProtocol address : addressList) {
            if (!lruHashMap.containsKey(address)) {
                lruHashMap.put(address, address);
            }
        }
        for (RpcProtocol existKey : lruHashMap.keySet()) {
            if (!addressList.contains(existKey)) {
                lruHashMap.remove(existKey);
            }
        }

        RpcProtocol eldestKey = lruHashMap.entrySet().iterator().next().getKey();
        return lruHashMap.get(eldestKey);
    }

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> rpcProtocols = serviceMap.get(serviceKey);
        if (rpcProtocols != null && rpcProtocols.size() > 0) {
            return doRoute(serviceKey, rpcProtocols);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
