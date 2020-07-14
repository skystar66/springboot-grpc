package com.xuliang.grpc.starter.model;
import com.xuliang.grpc.starter.constants.GrpcResponseStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xuliang
 */
@Data
public class GrpcResponse implements Serializable {

    private static final long serialVersionUID = -7161518426386434816L;

    /**
     * 响应状态
     */
    private int status;

    /**
     * 信息提示
     */
    private String message;

    /**
     * 返回结果
     */
    private Object result;

    /**
     * 服务端异常
     */
    private Throwable exception;

    /**
     * 异常堆栈信息
     */
    private StackTraceElement[] stackTrace;

    public void error(String message, Throwable exception, StackTraceElement[] stackTrace){
        this.status = GrpcResponseStatus.ERROR.getCode();
        this.message = message;
        this.exception = exception;
        this.stackTrace = stackTrace;
    }

    public void success(Object result){
        this.status = GrpcResponseStatus.SUCCESS.getCode();
        this.result = result;
    }

}
