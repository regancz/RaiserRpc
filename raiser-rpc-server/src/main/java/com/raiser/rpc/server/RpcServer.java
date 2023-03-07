package com.raiser.rpc.server;

import com.raiser.rpc.annotation.RpcService;
import com.raiser.rpc.server.core.NettyServer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author: zhengyangxin
 * @date: 9/5/2022 3:47 PM
 */
public class RpcServer extends NettyServer implements ApplicationContextAware, InitializingBean, DisposableBean {
    public RpcServer(String serverAddress, String registryAddress) {
        super(serverAddress, registryAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService nettyRpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String interfaceName = nettyRpcService.value().getName();
                String version = nettyRpcService.version();
                super.addService(interfaceName, version, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    @Override
    public void destroy() {
        super.stop();
    }
}
