package com.raiser.rpc.server.registry;

import com.raiser.rpc.config.Constant;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.protocol.RpcServiceInfo;
import com.raiser.rpc.util.ServiceUtil;
import com.raiser.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: zhengyangxin
 * @date: 8/30/2022 4:33 PM
 */
public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private CuratorClient curatorClient;
    private List<String> pathList = new ArrayList<>();

    public ServiceRegistry(String registryAddress) {
        curatorClient = new CuratorClient(registryAddress, 5000);
    }

    // create node and add listener
    public void registerService(String host, int port, Map<String, Object> serviceMap) {
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for (String key : serviceMap.keySet()) {
            String[] serviceInfo = key.split(ServiceUtil.makeServiceKey);
            if (serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setInterfaceName(serviceInfo[0]);
                if (serviceInfo.length == 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    // default set ""
                    rpcServiceInfo.setVersion("");
                }
                logger.info("Register new service: {}", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                logger.warn("Can not get service name and version: {}", key);
            }
        }
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfoList);

            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = Constant.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
            path = curatorClient.createPathData(path, bytes);
            pathList.add(path);
            logger.info("Register {} new service, host: {}, port: {}", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service fail, exception: {}", e.getMessage());
        }

        curatorClient.addConnectionStateListener((curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                registerService(host, port, serviceMap);
                logger.info("Connection state: {}, register service after reconnected", connectionState);
            }
        });
    }

    public void unregisterService() {
        for (String path : pathList) {
            try {
                curatorClient.deletePath(path);
            } catch (Exception e) {
                logger.error("Delete service path error: " + e.getMessage());
            }
        }
        curatorClient.close();
        logger.info("Unregister all service");
    }
}
