package rpc.filter.client;

import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;
import rpc.filter.ClientFilter;

import java.util.List;

/**
 * ip直连过滤器
 */
public class DirectInvokeFilterImpl implements ClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation){
        System.out.println("ip直连过滤器----未实现");
    }
}
