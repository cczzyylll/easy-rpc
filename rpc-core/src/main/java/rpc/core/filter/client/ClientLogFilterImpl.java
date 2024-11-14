package rpc.core.filter.client;

import rpc.core.common.ChannelFutureWrapper;
import rpc.core.common.RpcInvocation;
import rpc.core.filter.ClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.core.common.cache.CommonClientCache;

import java.util.List;

/**
 * @Author peng
 * @Date 2023/3/4
 * @description: 客户端日志记录过滤链路
 */
public class ClientLogFilterImpl implements ClientFilter {

    private final Logger logger = LoggerFactory.getLogger(ClientLogFilterImpl.class);

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        rpcInvocation.getAttachments().put("c_app_name", CommonClientCache.CLIENT_CONFIG.getApplicationName());
        logger.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -----> " +
                rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }

}