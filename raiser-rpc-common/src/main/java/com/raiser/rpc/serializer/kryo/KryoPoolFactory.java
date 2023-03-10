package com.raiser.rpc.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.raiser.rpc.codec.RpcRequest;
import com.raiser.rpc.codec.RpcResponse;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * @author: zhengyangxin
 * @date: 9/2/2022 9:15 PM
 */
public class KryoPoolFactory {
    private static volatile KryoPoolFactory poolFactory;

    private KryoFactory kryoFactory = () -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
        strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    };

    private KryoPool pool = new KryoPool.Builder(kryoFactory).build();

    private KryoPoolFactory() {
    }

    public static KryoPool getKryoInstance() {
        if (poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    private KryoPool getPool() {
        return pool;
    }
}
