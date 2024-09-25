package rpc.filter.client;

import org.slf4j.LoggerFactory;
import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;
import rpc.filter.ClientFilter;

import java.util.List;
import java.util.logging.Logger;

import static rpc.common.cache.CommonCilentCache.CLIENT_CONFIG;

/**
 * 客户端日志记录过滤链路
 */

public class ClientLogFilterImpl implements ClientFilter {
    private final Logger logger= (Logger) LoggerFactory.getLogger(ClientLogFilterImpl.class);
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation){
        rpcInvocation.getAttachments().put("clientApplicationName",CLIENT_CONFIG.getApplicationName());
        logger.info(rpcInvocation.getAttachments().get("clientApplicationName"+"do invoke ---=>")+
                rpcInvocation.getTargetServiceName()+"#"+rpcInvocation.getTargetMethod());

    }
}
