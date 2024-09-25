package rpc.client;

import rpc.proxy.ProxyFactory;

/**
 * rpc远程调用类
 */
public class RpcReference {
    public ProxyFactory proxyFactory;
    public RpcReference(ProxyFactory proxyFactory){
        this.proxyFactory=proxyFactory;
    }
    /**
     * 根据接口获取代理对象
     */
    public <T> T getProxyObject(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }
}
