package rpc.core.filter.server;

import rpc.core.common.RpcInvocation;
import rpc.core.common.ServerServiceSemaphoreWrapper;
import rpc.core.common.annotations.SPI;
import rpc.core.common.exception.MaxServiceLimitRequestException;
import rpc.core.filter.ServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.core.common.cache.CommonServerCache;

import java.util.concurrent.Semaphore;

/**
 * @Author peng
 * @Date 2023/3/11
 * @description: 服务端方法限流过滤器
 */
@SPI("before")
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceBeforeLimitFilterImpl.class);

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        if (!CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP.containsKey(serviceName)) {
            return;
        }
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        //从缓存中提取semaphore对象
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        boolean tryResult = semaphore.tryAcquire();
        if (!tryResult) {
            LOGGER.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException rpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(rpcException);
            throw rpcException;
        }
    }
}
