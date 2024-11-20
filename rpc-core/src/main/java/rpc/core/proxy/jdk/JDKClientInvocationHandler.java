package rpc.core.proxy.jdk;

import rpc.core.client.RpcReferenceWrapper;
import rpc.core.common.RpcInvocation;
import rpc.core.common.cache.CommonClientCache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static rpc.core.common.constants.RpcConstants.DEFAULT_TIMEOUT;


public class JDKClientInvocationHandler implements InvocationHandler {

    private final RpcReferenceWrapper<?> rpcReferenceWrapper;

    public JDKClientInvocationHandler(RpcReferenceWrapper<?> rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = initRpcInvocation(method, args);
        CommonClientCache.SEND_QUEUE.add(rpcInvocation);
        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }
        return sendRpcInvocation(rpcInvocation);
    }

    private RpcInvocation initRpcInvocation(Method method, Object[] args) {
        return RpcInvocation.builder()
                .targetMethod(method.getName())
                .targetServiceName(rpcReferenceWrapper.getAimClass().getName())
                .args(args)
                .uuid(UUID.randomUUID().toString())
                .retry(rpcReferenceWrapper.getRetry())
                .callSettings(rpcReferenceWrapper.getCallSettings())
                .build();
    }

    private Object sendRpcInvocation(RpcInvocation rpcInvocation) {
        long beginTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - beginTime) < rpcReferenceWrapper.getTimeOut() && rpcInvocation.getRetry() > 0) {
            Object object = CommonClientCache.RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                RpcInvocation rpcInvocationResp = (RpcInvocation) object;
                if (rpcInvocationResp.getE() != null && rpcInvocationResp.getRetry() > 0) {
                    retry(rpcInvocation);
                    beginTime = System.currentTimeMillis();
                } else {
                    CommonClientCache.RESP_MAP.remove(rpcInvocation.getUuid());
                    return rpcInvocationResp.getResponse();
                }
            }
        }
        CommonClientCache.RESP_MAP.remove(rpcInvocation.getUuid());
        if (rpcInvocation.getRetry() <= 0) {
            throw new RuntimeException("retry fail");
        } else {
            throw new RuntimeException("timeOut");
        }
    }

    private void retry(RpcInvocation rpcInvocation) {
        rpcInvocation.setE(null);
        rpcInvocation.setResponse(null);
        rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
        CommonClientCache.SEND_QUEUE.add(rpcInvocation);
    }


}
