package com.xuliang.grpc.starter.exception;

/**
 * @author xuliang
 */
public class GrpcException extends RuntimeException {

    public GrpcException(String message){
        super(message);
    }

}
