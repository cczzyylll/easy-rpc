package rpc.core.client;

import rpc.core.common.utils.CommonUtil;
import rpc.core.proxy.ProxyFactory;
import rpc.core.common.cache.CommonClientCache;

public class RpcReference {

    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 根据接口类型获取代理对象
     */
    public <T> T get(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        initGlobalRpcReferenceConfig(rpcReferenceWrapper);
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }

    private void initGlobalRpcReferenceConfig(RpcReferenceWrapper<?> rpcReferenceWrapper) {
        if (CommonUtil.isEmpty(rpcReferenceWrapper.getTimeOut())) {
            rpcReferenceWrapper.setTimeOut(CommonClientCache.CLIENT_CONFIG.getTimeOut());
        }
    }
}