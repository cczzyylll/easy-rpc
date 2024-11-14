package rpc.core.proxy;

import rpc.core.client.RpcReferenceWrapper;

/**
 * @Author peng
 * @Date 2023/2/24
 * @description: 代理工厂接口
 */
public interface ProxyFactory {


    <T> T getProxy(final RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable;
}
