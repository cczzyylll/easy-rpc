package rpc.core.proxy;

import rpc.core.client.RpcReferenceWrapper;

public interface ProxyFactory {


    <T> T getProxy(final RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable;
}
