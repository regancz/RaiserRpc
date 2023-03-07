package com.raiser.rpc.client.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @author: zhengyangxin
 * @date: 9/4/2022 2:10 PM
 */
public interface SerializableFunction<T> extends Serializable {
    default String getMethodName() throws Exception {
        Method method = this.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) method.invoke(this);
        return serializedLambda.getImplMethodName();
    }
}
