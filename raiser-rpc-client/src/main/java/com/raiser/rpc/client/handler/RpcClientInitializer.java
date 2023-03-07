package com.raiser.rpc.client.handler;

import com.raiser.rpc.codec.*;
import com.raiser.rpc.serializer.Serializer;
import com.raiser.rpc.serializer.protostuff.ProtostuffSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author: zhengyangxin
 * @date: 9/2/2022 10:17 PM
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Serializer serializer = ProtostuffSerializer.class.newInstance();
//        Serializer serializer = HessianSerializer.class.newInstance();
//        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        pipeline.addLast(new RpcEncoder(RpcRequest.class, serializer));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new RpcDecoder(RpcResponse.class, serializer));
        pipeline.addLast(new RpcClientHandler());
    }
}
