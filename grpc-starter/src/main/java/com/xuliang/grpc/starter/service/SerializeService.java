package com.xuliang.grpc.starter.service;
import com.google.protobuf.ByteString;
import com.xuliang.grpc.starter.model.GrpcRequest;
import com.xuliang.grpc.starter.model.GrpcResponse;
import com.xuliang.rpc.GrpcService;

/**
 * @author xuliang
 */
public interface SerializeService {

    /**
     * 序列化
     */
    ByteString serialize(GrpcResponse response);

    /**
     * 序列化
     */
    ByteString serialize(GrpcRequest request);

    /**
     * 反序列化
     */
    GrpcRequest deserialize(GrpcService.Request request);

    /**
     * 反序列化
     */
    GrpcResponse deserialize(GrpcService.Response response);

}
