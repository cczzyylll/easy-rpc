package rpc.filter;

import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;

import java.util.List;

public interface ClientFilter extends Filter{
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
