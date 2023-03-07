package com.raiser.rpc.client.route;

import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.protocol.RpcServiceInfo;
import com.raiser.rpc.util.ServiceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhengyangxin
 * @date: 9/1/2022 10:39 PM
 */
public abstract class RpcLoadBalance {
    protected Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectedServerNodes){
        Map<String, List<RpcProtocol>> serviceMap = new HashMap<>();
        if (connectedServerNodes != null && connectedServerNodes.size() > 0) {
            for (RpcProtocol rpcProtocol : connectedServerNodes.keySet()) {
                for (RpcServiceInfo rpcServiceInfo : rpcProtocol.getServiceInfoList()) {
                    String serviceKey = ServiceUtil.makeServiceKey(rpcServiceInfo.getInterfaceName(), rpcServiceInfo.getVersion());
                    List<RpcProtocol> rpcProtocolList = serviceMap.get(serviceKey);
                    if (rpcProtocolList == null) {
                        rpcProtocolList = new ArrayList<>();
                    }
                    rpcProtocolList.add(rpcProtocol);
                    serviceMap.putIfAbsent(serviceKey, rpcProtocolList);
                }
            }
        }
        return serviceMap;
    }

    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception;
}
