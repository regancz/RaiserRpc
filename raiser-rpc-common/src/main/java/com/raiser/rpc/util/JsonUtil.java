package com.raiser.rpc.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * @author: zhengyangxin
 * @date: 8/31/2022 11:53 AM
 */
public class JsonUtil {
    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        objMapper.setDateFormat(dateFormat);
        // 不为空的属性才会序列化
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 是否使用缩进，格式化输出
        objMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 是否自动关闭不属于生成器的底层输出流
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        // 是否自动补全json
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        // 流对象序列化之后是否强制刷新
        objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        // 若对象实现了CLOSEABLE接口，在序列化后是否调用Close方法
        objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        // 对象不含任何字段时是否报错
        objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 空对象不抛出异常
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 属性定义未找到是否报错
        objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    public static <T> byte[] serialize(T t) {
        byte[] bytes;
        try {
            bytes = objMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return bytes;
    }

    public static <T> T deserialize(byte[] bytes, Class<T> cls) {
        T t;
        try {
            t = objMapper.readValue(bytes, cls);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return t;
    }

    public static <T> T jsonToObject(String json, Class<?> cls) {
        T t;
        JavaType javaType = objMapper.getTypeFactory().constructType(cls);
        try {
            t = objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return t;
    }

    public static <T> T jsonToObjectList(String json, Class<?> collectionClass, Class<T> elementClass) {
        T t;
        JavaType javaType = objMapper.getTypeFactory().constructParametricType(collectionClass, elementClass);
        try {
            t = objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return t;
    }

    public static <T> T jsonToObjectHashMap(String json, Class<?> keyClass, Class<T> valueClass) {
        T t;
        JavaType javaType = objMapper.getTypeFactory().constructParametricType(HashMap.class, keyClass, valueClass);
        try {
            t = objMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return t;
    }

    public static String objectToJson(Object o) {
        String json;
        try {
            json = objMapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return json;
    }
}
