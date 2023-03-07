package com.raiser.rpc.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.raiser.rpc.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 5:03 PM
 */
public class KryoSerializer extends Serializer {
    private KryoPool kryoPool = KryoPoolFactory.getKryoInstance();

    @Override
    public <T> byte[] serialize(T obj) {
        Kryo kryo = kryoPool.borrow();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        try {
            kryo.writeObject(output, obj);
            output.close();
            return byteArrayOutputStream.toByteArray();
        } catch (KryoException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            kryoPool.release(kryo);
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = kryoPool.borrow();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        try {
            Object result = kryo.readObject(input, clazz);
            input.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            kryoPool.release(kryo);
        }
    }
}
