package rpc.core.common;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
public class RpcInvocation implements Serializable {
    private static final long serialVersionUID = 2951293262547830249L;
    private String targetMethod;
    private String targetServiceName;
    private Object[] args;
    private String uuid;
    private Object response;
    private Throwable e;
    private int retry;
    private Map<String, Object> callSettings;

}
