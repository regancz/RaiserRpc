package com.raiser.rpc.server.core;

import com.raiser.rpc.server.registry.ServiceRegistry;
import com.raiser.rpc.util.ServiceUtil;
import com.raiser.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author: zhengyangxin
 * @date: 8/30/2022 4:22 PM
 */
public class NettyServer extends Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Thread thread;
    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> serviceMap = new HashMap<>();
    private List<String> breakName = new ArrayList<>();

    public NettyServer(String serverAddress, String serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = new ServiceRegistry(serviceRegistry);
    }

    // creating node type is EPHEMERAL_SEQUENTIAL
    public void addService(String interfaceName, String version, Object serviceBean) {
        logger.info("Adding service, interface: {}, version: {}, bean: {}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
        breakName.add(serviceKey);
    }

    @Override
    public void start() {
        thread = new Thread(new Runnable() {
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(NettyServer.class.getSimpleName());

            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                NioEventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor, breakName))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE,true);

                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    ChannelFuture future = bootstrap.bind(host, port).sync();

                    if (serviceRegistry != null) {
                        serviceRegistry.registerService(host, port, serviceMap);
                    }
                    logger.info("Server started on port {}", port);
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (serviceRegistry != null) {
                            serviceRegistry.unregisterService();
                        }
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                        threadPoolExecutor.shutdown();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
