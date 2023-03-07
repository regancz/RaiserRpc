package com.raiser.rpc.server.core;

import com.raiser.rpc.codec.Beat;
import com.raiser.rpc.codec.RpcRequest;
import com.raiser.rpc.codec.RpcResponse;
import com.raiser.rpc.server.degrade.CircuitBreaker;
import com.raiser.rpc.server.degrade.Config;
import com.raiser.rpc.util.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import static com.raiser.rpc.server.degrade.State.*;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 4:33 PM
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private final Map<String, Object> handlerMap;
    private final ThreadPoolExecutor serverHandlerPool;
    private Map<String, CircuitBreaker> circuitBreakerList = new HashMap<>();

    public RpcServerHandler(Map<String, Object> handlerMap, ThreadPoolExecutor serverHandlerPool, List<String> breakName) {
        this.handlerMap = handlerMap;
        this.serverHandlerPool = serverHandlerPool;
        for (String s : breakName) {
            this.circuitBreakerList.put(s, new CircuitBreaker(new Config()));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        if (Beat.BEAT_ID.equalsIgnoreCase(rpcRequest.getRequestId())) {
            logger.info("Server read heartbeat ping");
            return;
        }

        CircuitBreaker breaker = circuitBreakerList.get(ServiceUtil.makeServiceKey(rpcRequest.getClassName(), rpcRequest.getVersion()));
        serverHandlerPool.execute(() -> {
            logger.info("Receive request " + rpcRequest.getRequestId());
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            try {
                if (breaker.state == OPEN) {
                    // 判断 是否超时可以进入 half-open状态
                    if (breaker.halfOpenTimeout()) {
                        Object result = halfOpenHandle(breaker, rpcRequest, breaker.fallback);
                        response.setResult(result);
                    } else {
                        // 直接执行失败回调方法
                        breaker.fallback.apply(new Exception("degrade by circuit breaker"));
                        // 如果降级关闭则正常执行
                    }
                } else if (breaker.state == CLOSED) {
                    Object result = handle(rpcRequest);
                    response.setResult(result);
                    // 注意重置 错误数
                    breaker.closed();
                } else if (breaker.state == HALF_OPEN) {
                    Object result = halfOpenHandle(breaker, rpcRequest, breaker.fallback);
                    response.setResult(result);
                }
            } catch (Throwable e) {
                breaker.open();
                breaker.fallback.apply(new Exception("degrade by circuit breaker"));
                response.setError(e.toString());
                logger.error("RPC Server handle request: {} error", rpcRequest.getRequestId());
            }
            channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    logger.info("Send response for request " + rpcRequest.getRequestId());
                }
            });
        });
    }

    private Object halfOpenHandle(CircuitBreaker breaker, RpcRequest rpcRequest , Function<Throwable, String> fallback) throws Throwable {
        try {
            // closed 状态超时进入 half-open 状态
            breaker.halfOpen();
            Object result = handle(rpcRequest);
            int halfOpenSuccCount = breaker.counter.incrSuccessHalfOpenCount();
            // half-open 状态成功次数到达阀值，进入 closed 状态
            if (halfOpenSuccCount >= breaker.config.getHalfOpenSuccessCount()) {
                breaker.closed();
            }
            return result;
        } catch (Exception e) {
            // half-open 状态发生一次错误进入 open 状态
            breaker.open();
            return fallback.apply(new Exception("degrade by circuit breaker"));
        }
    }

    private Object handle(RpcRequest rpcRequest) throws Throwable {
        String className = rpcRequest.getClassName();
        String version = rpcRequest.getVersion();
        String serviceKey = ServiceUtil.makeServiceKey(className, version);
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            logger.error("Can't find service implement with interface name: {}, version: {}",
                    className, version);
            return null;
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Object[] parameters = rpcRequest.getParameters();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        logger.debug(serviceClass.getName() + " : " + methodName + " is handling " +
                rpcRequest.getRequestId());

        // JDK reflect
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);

        // Cglib reflect
        FastClass serviceFastClass = FastClass.create(serviceClass);
//        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
//        return serviceFastMethod.invoke(serviceBean, parameters);
        // for higher-performance
        int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);

        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }
}
