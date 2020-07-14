package com.xuliang.grpc.starter.annotation;

import com.xuliang.grpc.starter.config.GrpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author xuliang
 * @auth:xuliang
 * @desc:grpc 自定义注解类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({GrpcAutoConfiguration.ExternalGrpcServiceScannerRegistrar.class})
public @interface GrpcServiceScan {

    /**
     * `@GrpcService` 所注解的包扫描路径
     */
    String[] packages() default {};

}