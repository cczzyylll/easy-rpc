package rpc.filter.client;

import rpc.common.ChannelFutureWrapper;
import rpc.common.RpcInvocation;
import rpc.filter.ClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端过滤链
 */

public class ClientFilterChain {
    private static List<ClientFilter> clientFilterList = new ArrayList<>();

    public void addClientFilter(ClientFilter clientFilter) {
        clientFilterList.add(clientFilter);
    }
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation){
        for(ClientFilter clientFilter:clientFilterList){
            clientFilter.doFilter(src,rpcInvocation);
        }
    }
}
