package com.raiser.rpc.serializer;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 4:57 PM
 */
public abstract class Serializer {
    public abstract <T> byte[] serialize(T obj);

    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
