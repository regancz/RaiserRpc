package com.raiser.rpc.serializer.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.raiser.rpc.serializer.Serializer;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 5:03 PM
 */
public class ProtostuffSerializer extends Serializer {
    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private Objenesis objenesis = new ObjenesisStd(true);

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> clazz) {
        return (Schema<T>) cachedSchema.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtostuffIOUtil.toByteArray(obj, getSchema(clazz), buffer);
        } catch (Exception e) {
            throw new IllegalStateException("Serialize failed: " + obj.getClass(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        T instance;
        try {
            instance = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, instance, schema);
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException("Deserialize failed: " + clazz, e);
        }
    }
}
