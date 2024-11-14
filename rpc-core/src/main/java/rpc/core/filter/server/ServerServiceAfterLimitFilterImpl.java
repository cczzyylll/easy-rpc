package rpc.core.filter.server;

import rpc.core.common.RpcInvocation;
import rpc.core.common.ServerServiceSemaphoreWrapper;
import rpc.core.common.annotations.SPI;
import rpc.core.filter.ServerFilter;
import rpc.core.common.cache.CommonServerCache;

/**
 * @Author peng
 * @Date 2023/3/11
 * @description: 服务端用于释放semaphore对象
 */
@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        if (!CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP.containsKey(serviceName)) {
            return;
        }
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }
}
