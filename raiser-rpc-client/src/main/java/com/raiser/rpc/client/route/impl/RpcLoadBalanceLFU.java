package com.raiser.rpc.client.route.impl;

import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.client.route.RpcLoadBalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: zhengyangxin
 * @date: 9/3/2022 4:56 PM
 */
public class RpcLoadBalanceLFU extends RpcLoadBalance {
    private ConcurrentMap<String, HashMap<RpcProtocol, Integer>> jobLfuMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    public RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLfuMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        HashMap<RpcProtocol, Integer> lfuItemMap = jobLfuMap.get(serviceKey);
        if (lfuItemMap == null) {
            lfuItemMap = new HashMap<>();
            jobLfuMap.putIfAbsent(serviceKey, lfuItemMap);
        }

        for (RpcProtocol address : addressList) {
            if (!lfuItemMap.containsKey(address) || lfuItemMap.get(address) > 1000000) {
                lfuItemMap.put(address, 0);
            }
        }

        for (RpcProtocol existKey : lfuItemMap.keySet()) {
            if (!addressList.contains(existKey)) {
                lfuItemMap.remove(existKey);
            }
        }

        List<Map.Entry<RpcProtocol, Integer>> lfuItemList = new ArrayList<>(lfuItemMap.entrySet());
        lfuItemList.sort(Map.Entry.comparingByValue());

        Map.Entry<RpcProtocol, Integer> addressItem = lfuItemList.get(0);
        RpcProtocol minAddress = addressItem.getKey();
        addressItem.setValue(addressItem.getValue() + 1);

        return minAddress;
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
