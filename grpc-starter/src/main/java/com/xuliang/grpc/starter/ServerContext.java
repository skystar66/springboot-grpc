package com.xuliang.grpc.starter;

import com.google.protobuf.ByteString;
import com.xuliang.grpc.starter.constants.SerializeType;
import com.xuliang.grpc.starter.model.GrpcRequest;
import com.xuliang.grpc.starter.model.GrpcResponse;
import com.xuliang.grpc.starter.service.SerializeService;
import com.xuliang.rpc.CommonServiceGrpc;
import com.xuliang.rpc.GrpcService;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuliang
 */
@Slf4j
public class ServerContext {

    private Channel channel;

    private final SerializeService defaultSerializeService;

    private CommonServiceGrpc.CommonServiceBlockingStub blockingStub;

    public ServerContext(Channel channel, SerializeService serializeService) {
        this.channel = channel;
        this.defaultSerializeService = serializeService;
        blockingStub = CommonServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 处理 gRPC 请求
     */
    public GrpcResponse handle(SerializeType serializeType, GrpcRequest grpcRequest) {
        ByteString bytes = defaultSerializeService.serialize(grpcRequest);
        int value = (serializeType == null ? -1 : serializeType.getValue());
        GrpcService.Request request = GrpcService.Request.newBuilder().setSerialize(value).setRequest(bytes).build();
        GrpcService.Response response = null;
        try{
            response = blockingStub.handle(request);
        }catch (Exception exception){
            log.warn("rpc exception: {}", exception.getMessage());
            if ("UNAVAILABLE: io exception".equals(exception.getMessage().trim())){
                response = blockingStub.handle(request);
            }
        }
        return defaultSerializeService.deserialize(response);
    }
}
