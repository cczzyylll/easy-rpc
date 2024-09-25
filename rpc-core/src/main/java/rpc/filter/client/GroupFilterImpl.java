package rpc.filter.client;

import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;
import rpc.filter.ClientFilter;

import java.util.List;

/**
 * 服务分组过滤链路
 */
public class GroupFilterImpl implements ClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation){
        System.out.println("组过滤---还未实现");
    }

}
