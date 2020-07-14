package com.xuliang.grpc.starter.binding;

import com.xuliang.grpc.starter.client.GrpcClient;
import com.xuliang.grpc.starter.annotation.GrpcService;
import com.xuliang.grpc.starter.constants.GrpcResponseStatus;
import com.xuliang.grpc.starter.constants.SerializeType;
import com.xuliang.grpc.starter.exception.GrpcException;
import com.xuliang.grpc.starter.model.GrpcRequest;
import com.xuliang.grpc.starter.model.GrpcResponse;
import org.springframework.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author xuliang
 */
public class GrpcServiceProxy<T> implements InvocationHandler {


    private Class<T> grpcService;

    private Object invoker;


    public GrpcServiceProxy(Class<T> grpcService, Object invoker) {
        this.grpcService = grpcService;
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        String className = grpcService.getName();
        if ("toString".equals(methodName) && args.length == 0) {
            return className + "@" + invoker.hashCode();
        } else if ("hashCode".equals(methodName) && args.length == 0) {
            return invoker.hashCode();
        } else if ("equals".equals(methodName) && args.length == 1) {
            Object another = args[0];
            return proxy == another;
        }
        /**获取grpc的bean*/
        GrpcService annotation = grpcService.getAnnotation(GrpcService.class);
        /**获取远程服务名*/
        String server = annotation.server();
        /**请求*/
        GrpcRequest request = new GrpcRequest();
        request.setClazz(className);
        request.setMethod(methodName);
        request.setArgs(args);
        SerializeType[] serializeTypeArray = annotation.serialization();
        SerializeType serializeType = null;
        if (serializeTypeArray.length > 0) {
            serializeType = serializeTypeArray[0];
        }
        GrpcResponse response = GrpcClient.connect(server).handle(serializeType, request);
        if (GrpcResponseStatus.ERROR.getCode() == response.getStatus()) {
            Throwable throwable = response.getException();
            GrpcException exception = new GrpcException(throwable.getClass().getName() + ": " + throwable.getMessage());
            StackTraceElement[] exceptionStackTrace = exception.getStackTrace();
            StackTraceElement[] responseStackTrace = response.getStackTrace();
            StackTraceElement[] allStackTrace = Arrays.copyOf(exceptionStackTrace,
                    exceptionStackTrace.length + responseStackTrace.length);
            System.arraycopy(responseStackTrace, 0, allStackTrace, exceptionStackTrace.length, responseStackTrace.length);
            exception.setStackTrace(allStackTrace);
            throw exception;
        }
        return response.getResult();
    }
}
