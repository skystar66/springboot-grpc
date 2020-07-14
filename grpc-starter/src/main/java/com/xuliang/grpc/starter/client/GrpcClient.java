package com.xuliang.grpc.starter.client;

import com.xuliang.grpc.starter.ServerContext;
import com.xuliang.grpc.starter.config.GrpcProperties;
import com.xuliang.grpc.starter.config.RemoteServer;
import com.xuliang.grpc.starter.service.SerializeService;
import io.grpc.*;
import io.grpc.internal.DnsNameResolverProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuliang
 */
@Slf4j
public class GrpcClient {


    private static final Map<String, ServerContext> serverMap = new HashMap<>();


    private final GrpcProperties grpcProperties;

    private final SerializeService serializeService;


    private ClientInterceptor clientInterceptor;

    public GrpcClient(GrpcProperties grpcProperties, SerializeService serializeService) {
        this.grpcProperties = grpcProperties;
        this.serializeService = serializeService;
    }


    /**
     * 初始化
     */
    public void init() {
        /**获取远程服务信息*/
        List<RemoteServer> remoteServers = grpcProperties.getRemoteServers();

        if (!CollectionUtils.isEmpty(remoteServers)) {
            for (RemoteServer remoteServer : remoteServers) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(remoteServer.getHost(), remoteServer.getPort())
                        .defaultLoadBalancingPolicy("round_robin")
                        .nameResolverFactory(new DnsNameResolverProvider())
                        .idleTimeout(30, TimeUnit.SECONDS)
                        .usePlaintext()
                        .build();

                if (clientInterceptor != null) {
                    Channel newChannel = ClientInterceptors.intercept(channel, clientInterceptor);
                    serverMap.put(remoteServer.getServer(), new ServerContext(newChannel, serializeService));

                } else {
                    Class clazz = grpcProperties.getClientInterceptor();
                    if (clazz == null) {
                        serverMap.put(remoteServer.getServer(), new ServerContext(channel, serializeService));
                    } else {
                        try {
                            ClientInterceptor interceptor = (ClientInterceptor) clazz.newInstance();
                            Channel newChannel = ClientInterceptors.intercept(channel, interceptor);
                            serverMap.put(remoteServer.getServer(), new ServerContext(newChannel, serializeService));
                        } catch (InstantiationException | IllegalAccessException e) {
                            log.warn("ClientInterceptor cannot use, ignoring...");
                            serverMap.put(remoteServer.getServer(), new ServerContext(channel, serializeService));
                        }
                    }
                }
            }
        }
    }

    /**
     * 连接远程服务
     */
    public static ServerContext connect(String serverName) {
        return serverMap.get(serverName);
    }

}
