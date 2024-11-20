package rpc.core.filter.server;

import rpc.core.common.RpcInvocation;
import rpc.core.common.annotations.SPI;
import rpc.core.common.utils.CommonUtil;
import rpc.core.filter.ServerFilter;
import rpc.core.server.ServiceWrapper;
import rpc.core.common.cache.CommonServerCache;

/**
 * @Author peng
 * @Date 2023/3/4
 * @description: 简单版本的token校验
 */
@SPI("before")
public class ServerTokenFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getCallSettings().get("serviceToken"));
        if (!CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP.containsKey(rpcInvocation.getTargetServiceName())) {
            return;
        }
        ServiceWrapper serviceWrapper = CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if (CommonUtil.isEmpty(matchToken)) return;
        if (CommonUtil.isNotEmpty(token) && token.equals(matchToken)) return;
        throw new RuntimeException("token is " + token + " , verify result is false!");
    }
}