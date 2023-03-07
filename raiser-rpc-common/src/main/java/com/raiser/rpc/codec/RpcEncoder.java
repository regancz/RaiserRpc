package com.raiser.rpc.codec;

import com.raiser.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 6:35 PM
 */
public class RpcEncoder extends MessageToByteEncoder {
    private static Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RpcEncoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            try {
                byte[] bytes = serializer.serialize(o);
                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);
            } catch (Exception e) {
                logger.error("Encode error: " + e);
            }
        }
    }
}
