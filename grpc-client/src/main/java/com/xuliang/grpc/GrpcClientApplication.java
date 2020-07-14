package com.xuliang.grpc;

import com.xuliang.grpc.starter.annotation.GrpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author xuliang
 */
@SpringBootApplication
@GrpcServiceScan(packages = {"com.xuliang.grpc.facade"})
public class GrpcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcClientApplication.class, args);
    }
}
