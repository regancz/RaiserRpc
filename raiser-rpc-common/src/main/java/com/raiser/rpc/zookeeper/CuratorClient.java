package com.raiser.rpc.zookeeper;

import com.raiser.rpc.config.Constant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * @author: zhengyangxin
 * @date: 8/30/2022 9:39 PM
 */
public class CuratorClient {
    private CuratorFramework client;

    public CuratorClient(String connectString, String namespace, int sessionTimeout, int connectionTimeout) {
        client = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        client.start();
    }

    public CuratorClient(String connectString, int timeout) {
        this(connectString, Constant.ZK_NAMESPACE, timeout, timeout);
    }
    public CuratorClient(String connectString) {
        this(connectString, Constant.ZK_NAMESPACE, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
    }

    public CuratorFramework getClient() {
        return client;
    }

    public String createPathData(String path, byte[] data) throws Exception {
        // use creatingParentContainersIfNeeded instead of creatingParentsIfNeeded
        return client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data);
    }

    public void updatePathData(String path, byte[] data) throws Exception {
        client.setData().forPath(path, data);
    }

    public void deletePath(String path) throws Exception {
        client.delete().forPath(path);
    }

    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
        client.getConnectionStateListenable().addListener(connectionStateListener);
    }

    public void watchNode(String path, Watcher watcher) throws Exception {
        client.getData().usingWatcher(watcher).forPath(path);
    }

    public void watchPathChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    public void close() {
        client.close();
    }

}
