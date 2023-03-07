package com.raiser.rpc.client.connect;

import com.raiser.rpc.client.handler.RpcClientHandler;
import com.raiser.rpc.client.handler.RpcClientInitializer;
import com.raiser.rpc.protocol.RpcProtocol;
import com.raiser.rpc.client.route.RpcLoadBalance;
import com.raiser.rpc.client.route.impl.RpcLoadBalanceRoundRobin;
import com.raiser.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: zhengyangxin
 * @date: 9/1/2022 9:06 PM
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeClientThreadPool();

    private Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private long waitTimeout = 5000;
    private RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();
    private volatile boolean isRunning = true;

    private ConnectionManager(){}

    private static class SingletonHolder {
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance(){
        return SingletonHolder.instance;
    }

    public void updateConnectedServer(List<RpcProtocol> serverList) {
        if (serverList != null && serverList.size() > 0) {
            HashSet<RpcProtocol> serviceSet = new HashSet<>(serverList.size());
            serviceSet.addAll(serverList);

            for (final RpcProtocol rpcProtocol : serviceSet) {
                if (!rpcProtocolSet.contains(rpcProtocol)) {
                    connectServerNode(rpcProtocol);
                }
            }

            for (RpcProtocol rpcProtocol : rpcProtocolSet) {
                if (!serviceSet.contains(rpcProtocol)) {
                    logger.info("Remove invalid service: " + rpcProtocol.toJson());
                    removeAndCloseHandler(rpcProtocol);
                }
            }
        } else {
            logger.error("No available service");
            for (RpcProtocol rpcProtocol : rpcProtocolSet) {
                removeAndCloseHandler(rpcProtocol);
            }
        }
    }

    private void removeAndCloseHandler(RpcProtocol rpcProtocol) {
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            handler.close();
        }
        connectedServerNodes.remove(rpcProtocol);
        rpcProtocolSet.remove(rpcProtocol);
    }

    private void connectServerNode(RpcProtocol rpcProtocol) {
        if (rpcProtocol.getServiceInfoList() == null || rpcProtocol.getServiceInfoList().isEmpty()) {
            logger.info("No service on node, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
            return;
        }
        rpcProtocolSet.add(rpcProtocol);
        logger.info("New service node, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
        // InetSocketAddress is used in proxy model
        // if unresolved, it still can be used
        final InetSocketAddress address = new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
        threadPoolExecutor.submit(() -> {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer());

            ChannelFuture channelFuture = b.connect(address);
            channelFuture.addListener(future -> {
                if (channelFuture.isSuccess()) {
                    logger.info("Successfully connect to remote server, remote peer = " + address);
                    RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                    connectedServerNodes.put(rpcProtocol, handler);
                    handler.setRpcProtocol(rpcProtocol);
                    signalAvailableHandler();
                } else {
                    logger.error("Can not connect to remote server, remote peer = " + address);
                }
            });
        });
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        isRunning = false;
        for (RpcProtocol rpcProtocol : rpcProtocolSet) {
            removeAndCloseHandler(rpcProtocol);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

    public void removeHandler(RpcProtocol rpcProtocol) {
        rpcProtocolSet.remove(rpcProtocol);
        connectedServerNodes.remove(rpcProtocol);
        logger.info("Remove connection, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                logger.error("Waiting for available service is interrupted!", e);
            }
        }
        RpcProtocol rpcProtocol = loadBalance.route(serviceKey, connectedServerNodes);
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            return handler;
        } else {
            throw new Exception("Can not get available connection");
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            logger.warn("Waiting for available service");
            return condition.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
        if (rpcProtocol == null) {
            return;
        }
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcProtocolSet.contains(rpcProtocol)) {
            connectServerNode(rpcProtocol);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            //TODO We may don't need to reconnect remote server if the server'IP and server'port are not changed
            removeAndCloseHandler(rpcProtocol);
            connectServerNode(rpcProtocol);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            removeAndCloseHandler(rpcProtocol);
        } else {
            throw new IllegalArgumentException("Unknown type:" + type);
        }
    }

}
