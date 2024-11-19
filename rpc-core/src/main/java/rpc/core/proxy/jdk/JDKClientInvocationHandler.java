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

    private final static Object OBJECT = new Object();

    private int timeOut = DEFAULT_TIMEOUT;

    private final RpcReferenceWrapper<?> rpcReferenceWrapper;

    public JDKClientInvocationHandler(RpcReferenceWrapper<?> rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
        timeOut = Integer.parseInt(rpcReferenceWrapper.getTimeOut());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = initRpcInvocation(method, args);
        CommonClientCache.RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        CommonClientCache.SEND_QUEUE.add(rpcInvocation);
        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < timeOut || rpcInvocation.getRetry() > 0) {
            Object object = CommonClientCache.RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                RpcInvocation rpcInvocationResp = (RpcInvocation) object;
                //异常结果+有重试次数=异常重试
                if (rpcInvocationResp.getE() != null && rpcInvocationResp.getRetry() > 0) {
                    //重新请求
                    rpcInvocation.setE(null);
                    rpcInvocation.setResponse(null);
                    rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                    CommonClientCache.RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                    CommonClientCache.SEND_QUEUE.add(rpcInvocation);
                    beginTime = System.currentTimeMillis();
                } else {
                    CommonClientCache.RESP_MAP.remove(rpcInvocation.getUuid());
                    return rpcInvocationResp.getResponse();
                }
            }
            //超时重试
            if (System.currentTimeMillis() - beginTime > timeOut) {
                //重新请求
                rpcInvocation.setResponse(null);
                rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                CommonClientCache.RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                CommonClientCache.SEND_QUEUE.add(rpcInvocation);
                beginTime = System.currentTimeMillis();
            }
        }
        CommonClientCache.RESP_MAP.remove(rpcInvocation.getUuid());
        throw new TimeoutException("client wait server's response timeout!");
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

    private void



}
