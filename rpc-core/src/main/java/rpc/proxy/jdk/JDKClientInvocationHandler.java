package rpc.proxy.jdk;


import rpc.client.RpcReferenceWrapper;
import rpc.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static rpc.common.cache.CommonCilentCache.RESP_MAP;
import static rpc.common.cache.CommonCilentCache.SEND_QUEUE;

/**
 * 动态代理调用逻辑
 */

public class JDKClientInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();
    private RpcReferenceWrapper<?> rpcReferenceWrapper;
    private int timeOut = 100;
    public JDKClientInvocationHandler(RpcReferenceWrapper<?> rpcReferenceWrapper){
        this.rpcReferenceWrapper=rpcReferenceWrapper;
    }
    @Override
    public Object invoke(Object proxy, Method method,Object[] args)throws Throwable{
        RpcInvocation rpcInvocation=new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttatchments());
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setRetry(rpcReferenceWrapper.getRetry());
        RESP_MAP.put(rpcInvocation.getUuid(),OBJECT);
        SEND_QUEUE.put(rpcInvocation);
        if(rpcReferenceWrapper.isAsync()){
            return null;
        }
        long beginTime=System.currentTimeMillis();
        while (System.currentTimeMillis()-beginTime<timeOut||rpcInvocation.getRetry()>0){
            Object object=RESP_MAP.get(rpcInvocation.getUuid());
            if(object instanceof RpcInvocation){
                RpcInvocation rpcInvocationResp=(RpcInvocation) object;
                if(rpcInvocationResp.getE()!=null&& rpcInvocationResp.getRetry()>0){
                    rpcInvocation.setE(null);
                    rpcInvocation.setResponse(null);
                    rpcInvocation.setRetry(rpcInvocation.getRetry()-1);
                    RESP_MAP.put(rpcInvocation.getUuid(),OBJECT);
                    SEND_QUEUE.add(rpcInvocation);
                    beginTime=System.currentTimeMillis();
                }
                else {
                    RESP_MAP.remove(rpcInvocation.getUuid());
                    return rpcInvocationResp.getResponse();
                }
                //超时重试
                if (System.currentTimeMillis() - beginTime > timeOut) {
                    rpcInvocation.setResponse(null);
                    rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                    RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                    SEND_QUEUE.add(rpcInvocation);
                    beginTime = System.currentTimeMillis();
                }
            }
        }
        RESP_MAP.remove(rpcInvocation.getUuid());
        throw new TimeoutException("client wait server's response timeout!");
    }
}
