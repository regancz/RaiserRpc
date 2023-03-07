package com.raiser.rpc.server.core;

import com.raiser.rpc.codec.*;
import com.raiser.rpc.serializer.protostuff.ProtostuffSerializer;
import com.raiser.rpc.serializer.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 4:33 PM
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> handlerMap;
    private ThreadPoolExecutor executor;
    private List<String> breakName;

    public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor executor, List<String> breakName) {
        this.handlerMap = handlerMap;
        this.executor = executor;
        this.breakName = breakName;
    }

    @Override
    // support three ways to serialize, protobuf, hessian and kryo
    protected void initChannel(SocketChannel serverChannel) throws Exception {
        Serializer serializer = ProtostuffSerializer.class.newInstance();
//        Serializer serializer = HessianSerializer.class.newInstance();
//        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline channelPipeline = serverChannel.pipeline();
        channelPipeline.addLast(new IdleStateHandler(0, 0 , Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        // solve tcp sticky packet
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        channelPipeline.addLast(new RpcDecoder(RpcRequest.class, serializer));
        channelPipeline.addLast(new RpcEncoder(RpcResponse.class, serializer));
        channelPipeline.addLast(new RpcServerHandler(handlerMap, executor, breakName));
    }
}