package com.xuliang.grpc.starter.annotation;


import com.xuliang.grpc.starter.constants.SerializeType;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * @auth:xuliang
 * @desc:grpc 自定义注解类
 */
@Documented
@Inherited
@Retention(RUNTIME)
public @interface GrpcService {

    /**
     * 远程服务名
     */
    String server() default "";

    /**
     * 序列化工具实现类
     */
    SerializeType[] serialization() default {};
}
