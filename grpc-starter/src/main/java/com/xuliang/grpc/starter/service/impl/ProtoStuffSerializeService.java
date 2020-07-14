package com.xuliang.grpc.starter.service.impl;

import com.google.protobuf.ByteString;
import com.xuliang.grpc.starter.model.GrpcRequest;
import com.xuliang.grpc.starter.model.GrpcResponse;
import com.xuliang.grpc.starter.service.SerializeService;
import com.xuliang.grpc.starter.util.ProtobufUtils;
import com.xuliang.rpc.GrpcService;

/**
 * ProtoStuff 序列化/反序列化工具
 */
public class ProtoStuffSerializeService implements SerializeService {

    @Override
    public GrpcRequest deserialize(GrpcService.Request request) {
        return ProtobufUtils.deserialize(request.getRequest().toByteArray(), GrpcRequest.class);
    }

    @Override
    public GrpcResponse deserialize(GrpcService.Response response) {
        return ProtobufUtils.deserialize(response.getResponse().toByteArray(), GrpcResponse.class);
    }

    @Override
    public ByteString serialize(GrpcResponse response) {
        return ByteString.copyFrom(ProtobufUtils.serialize(response));
    }

    @Override
    public ByteString serialize(GrpcRequest request) {
        return  ByteString.copyFrom(ProtobufUtils.serialize(request));
    }

}
