package rpc.filter.server;

import rpc.common.RpcInvocation;
import rpc.filter.ServerFilter;

import java.util.ArrayList;
import java.util.List;

public class ServerAfterFilterChain {
    private List<ServerFilter> serverFilters=new ArrayList<>();
    public void add(ServerFilter serverFilter){
        serverFilters.add(serverFilter);
    }
    public void doFilter(RpcInvocation rpcInvocation){
        for(ServerFilter serverFilter:serverFilters){
            serverFilter.doFilter(rpcInvocation);
        }
    }
}
