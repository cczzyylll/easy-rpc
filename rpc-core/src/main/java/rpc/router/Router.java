package rpc.router;

import rpc.common.ChannelFutureWrapper;

public interface Router {
    void refreshRouterArr(Selector selector);
    ChannelFutureWrapper select(ChannelFutureWrapper[] channelFutureWrappers);
}
