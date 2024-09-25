package rpc.proxy;

import rpc.client.RpcReferenceWrapper;

public interface ProxyFactory {
    <T> T getProxy( RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable;
}
