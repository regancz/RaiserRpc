package com.raiser.rpc.client;

import com.raiser.rpc.annotation.RpcAutowired;
import com.raiser.rpc.client.connect.ConnectionManager;
import com.raiser.rpc.client.discovery.ServiceDiscovery;
import com.raiser.rpc.client.proxy.RpcService;
import com.raiser.rpc.client.proxy.ServiceInvocationHandler;
import com.raiser.rpc.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: zhengyangxin
 * @date: 9/1/2022 6:10 PM
 */
public class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeClientThreadPool();

    public RpcClient(String address) {
        this.serviceDiscovery = new ServiceDiscovery(address);
    }

    @SuppressWarnings("unchecked")
    public static <T, P> T createService(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ServiceInvocationHandler<T, P>(interfaceClass, version)
        );
    }

    public static <T, P> RpcService createAsyncService(Class<T> interfaceClass, String version) {
        return new ServiceInvocationHandler<T, P>(interfaceClass, version);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    @Override
    public void destroy() {
        this.stop();
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try {
                for (Field field : fields) {
                    RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
                    if (rpcAutowired != null) {
                        String version = rpcAutowired.version();
                        field.setAccessible(true);
                        field.set(bean, createService(field.getType(), version));
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(e.toString());
            }
        }
    }
}
