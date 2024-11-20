package rpc.core.filter.client;

import rpc.core.common.ChannelFutureWrapper;
import rpc.core.common.RpcInvocation;
import rpc.core.common.utils.CommonUtil;
import rpc.core.filter.ClientFilter;

import java.util.List;


public class GroupFilterImpl implements ClientFilter {

    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getCallSettings().get("group"));
        src.removeIf(channelFutureWrapper -> !channelFutureWrapper.getGroup().equals(group));
        if (CommonUtil.isEmptyList(src)) {
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}