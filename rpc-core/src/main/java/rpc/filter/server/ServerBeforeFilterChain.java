package rpc.filter.server;

import rpc.common.RpcInvocation;
import rpc.filter.ServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器端前置过滤链
 */

public class ServerBeforeFilterChain {
    private static List<ServerFilter> serverFilters=new ArrayList<>();
    public void addServerFilter(ServerFilter serverFilter){
        serverFilters.add(serverFilter);
    }
    public void doFilter(RpcInvocation rpcInvocation){
        for(ServerFilter serverFilter:serverFilters){
            serverFilter.doFilter(rpcInvocation);
        }
    }
}
