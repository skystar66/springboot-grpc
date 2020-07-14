package com.xuliang.grpc.starter.server;

import com.xuliang.grpc.starter.config.GrpcProperties;
import com.xuliang.grpc.starter.service.CommonService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author xuliang
 */
@Slf4j
public class GrpcServer implements DisposableBean {


    private final GrpcProperties grpcProperties;

    private final CommonService commonService;

    private ServerInterceptor serverInterceptor;

    private Server server;


    public GrpcServer(GrpcProperties grpcProperties, CommonService commonService) {
        this.grpcProperties = grpcProperties;
        this.commonService = commonService;
    }


    public void start() throws IOException, IllegalAccessException, InstantiationException {
        int port = grpcProperties.getPort();
        if (serverInterceptor != null) {
            server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(commonService, serverInterceptor)).build().start();
        } else {
            Class clazz = grpcProperties.getServerInterceptor();
            if (clazz == null) {
                server = ServerBuilder.forPort(port).addService(commonService).build().start();
            } else {
                server = ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(commonService, (ServerInterceptor) clazz.newInstance())).build().start();
            }
        }
        log.info("gRPC Server started, listening on " + port);
        startDaemonAwaitThread();
    }


    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(() -> {
            try {
                GrpcServer.this.server.awaitTermination();
            } catch (InterruptedException e) {
                log.warn("gRPC server stopped." + e.getMessage());
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    /**
     * 销毁
     */
    @Override
    public void destroy() {
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        log.info("gRPC server stopped.");
    }

}
