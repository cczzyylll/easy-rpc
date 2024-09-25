package rpc.proxy.jdk;


import rpc.client.RpcReferenceWrapper;
import rpc.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

public class JDKProxyFactory implements ProxyFactory{
    @Override
    public <T> T getProxy(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable{
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(),new Class[]{rpcReferenceWrapper.getAimClass()},new JDKClientInvocationHandler(rpcReferenceWrapper));
    }
}
