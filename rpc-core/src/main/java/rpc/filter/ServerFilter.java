package rpc.filter;

import rpc.common.RpcInvocation;

public interface ServerFilter extends Filter{
    void doFilter(RpcInvocation rpcInvocation);
}
