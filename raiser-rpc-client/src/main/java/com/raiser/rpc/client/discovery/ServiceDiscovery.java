package com.raiser.rpc.client.discovery;

import com.raiser.rpc.client.connect.ConnectionManager;
import com.raiser.rpc.config.Constant;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhengyangxin
 * @date: 9/1/2022 8:28 PM
 */
public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private CuratorClient curatorClient;

    public ServiceDiscovery(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress);
        discoveryService();
    }

    private void discoveryService() {
        try {
            logger.info("Get initial service info");
            getServiceAndUpdateServer();
            // once register ZK_REGISTRY_PATH, listener can be triggered every time
            curatorClient.watchPathChildrenNode(Constant.ZK_REGISTRY_PATH, (curatorFramework, pathChildrenCacheEvent) -> {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                ChildData childData = pathChildrenCacheEvent.getData();
                switch (type) {
                    case CONNECTION_RECONNECTED:
                        logger.info("Reconnected to zk, try to get latest service list");
                        getServiceAndUpdateServer();
                        break;
                    case CHILD_ADDED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                        break;
                    case CHILD_UPDATED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                        break;
                    case CHILD_REMOVED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                        break;
                }
            });
        } catch (Exception e) {
            logger.error("Watch node exception: " + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer() {
        try {
            List<String> nodeList = curatorClient.getChildren(Constant.ZK_REGISTRY_PATH);
            List<RpcProtocol> serviceList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = curatorClient.getData(Constant.ZK_REGISTRY_PATH + "/" + node);
                RpcProtocol rpcProtocol = RpcProtocol.fromJson(new String(bytes));
                serviceList.add(rpcProtocol);
                logger.debug("Service add node: " + node);
            }
            logger.debug("Service node data: {}", serviceList);
            updateConnectedServer(serviceList);
        } catch (Exception e) {
            logger.error("Get node exception: " + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        logger.info("Child data updated, path: {}, type: {}, data: {}", path, type, path);
        RpcProtocol rpcProtocol = RpcProtocol.fromJson(data);
        updateConnectedServer(rpcProtocol, type);
    }

    private void updateConnectedServer(List<RpcProtocol> dataList) {
        ConnectionManager.getInstance().updateConnectedServer(dataList);
    }

    private void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
        ConnectionManager.getInstance().updateConnectedServer(rpcProtocol, type);
    }

    public void stop() {
        this.curatorClient.close();
    }
}
