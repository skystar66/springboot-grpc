package com.xuliang.grpc.starter.constants;

import com.xuliang.grpc.starter.service.impl.ProtoStuffSerializeService;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化类型枚举
 * @author xuliang
 */
public enum SerializeType {

    PROTOSTUFF(1, ProtoStuffSerializeService.class),;

    private static Map<Integer, SerializeType> enumMap = new HashMap<>();

    static {
        for (SerializeType serializeType : SerializeType.values()) {
            enumMap.put(serializeType.value, serializeType);
        }
    }

    private int value;

    private Class clazz;

    SerializeType(int value, Class clazz){
        this.clazz = clazz;
        this.value = value;
    }

    public static SerializeType getSerializeTypeByValue(int value){
        return enumMap.get(value);
    }

    public int getValue() {
        return value;
    }

    public Class getClazz() {
        return clazz;
    }
}
