package rpc.core.proxy.jdk;

import rpc.core.client.RpcReferenceWrapper;
import rpc.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

public class JDKProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(),
                rpcReferenceWrapper.getAimClass().getInterfaces(),
                new JDKClientInvocationHandler(rpcReferenceWrapper));
    }

}