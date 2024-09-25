package rpc.filter.server;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import rpc.common.RpcInvocation;
import rpc.common.annotations.SPI;
import rpc.filter.ServerFilter;


/***
 * 服务端日志过滤器
 */
@SPI(value = "before")
public class ServerLogFilterImpl implements ServerFilter {
    Logger logger=LoggerFactory.getLogger(ServerLogFilterImpl.class);
    @Override
    public void doFilter(RpcInvocation rpcInvocation){
        logger.info(rpcInvocation.getAttachments().get("clientApplicationName")+"  do invoke ------>"+rpcInvocation.getTargetServiceName()+"#"+rpcInvocation.getTargetMethod());
    }
}
